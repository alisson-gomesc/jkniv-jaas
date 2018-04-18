/* 
 * JKNIV ,
 * Copyright (C) 2017, the original author or authors.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.sf.jkniv.jaas.gf;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.security.auth.login.LoginException;

import com.sun.enterprise.deployment.UserDataConstraintImpl;
import com.sun.enterprise.security.auth.realm.BadRealmException;
import com.sun.enterprise.security.auth.realm.NoSuchRealmException;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.logging.LogDomains;

class LdapAdapter
{
    private static final Logger         LOG                          = MyLoggerFactory.getLogger(LdapAdapter.class);
    
    // find the pattern CN=...
    private static final String         REGEX_COMMON_NAME            = "CN=[\\w\\.?]+";                             // CN=my_group,OU=dev,OU=acme,DC=acme,DC=com,DC=br   ---> CN=my_group
    public static final Pattern         PATTERN_CN                   = Pattern.compile(REGEX_COMMON_NAME,
            Pattern.CASE_INSENSITIVE);
    
    /** LDAP URL for your server */
    public static final String          PROP_DIRURL                  = "directories";
    
    /* LDAP base DN for the location of user data   */
    //public static final String         PROP_BASEDN                  = "base-dn";
    
    /** security level to use "none", "simple", "strong". */
    public static final String          PROP_SECURITY_AUTHENTICATION = "auth-level";
    
    /** Default domain from users user@mydomain.com */
    public static final String          PROP_DEFAULT_DOMAIN          = "default-domain";
    
    /** Attribute name thats representgroup-member-attr group of user <b>group-member-attr</b> */
    public static final String          PROP_ATTR_GROUP_MEMBER       = "group-member-attr";
    
    public static final String          DEFAULT_AUTH                 = "simple";
    
    public static final String          DEFAULT_FETCH_ATTR           = "memberOf";
    private static final String         DEFAULT_REFERRAL             = "follow";
 
    private static final String         PROP_FORCE_AUTH_LDAP               = "force-auth-ldap";
    
    private static final String         URL_LDAP                     = "ldap://";
    private static final String         URL_LDAPS                    = "ldaps://";
    private static final String         DEFAULT_POOL_PROTOCOL        = "plain ssl";
    private static final String         SSL                          = "SSL";
    
    private static final String         PORT_SSL                     = "636";
    private static final String         PORT                         = "389";
    
    // --------------------------------------------------------------------------------------------- //
    
    // These are optional, defaults are provided
    // %s = subject name
    // %d = DN of user search result
    public static final String          PROP_SEARCH_FILTER           = "search-filter";
    public static final String          PROP_JNDICF                  = "jndiCtxFactory";
    public static final String          PROP_READ_TIMEOUT            = "read.timeout";
    // Expansion strings
    public static final String          SUBST_SUBJECT_NAME           = "%s";
    public static final String          SUBST_SUBJECT_DN             = "%d";
    
    // Defaults
    private static final String         DEFAULT_SEARCH_FILTER        = "mail=" + SUBST_SUBJECT_NAME;
    private static final String         DEFAULT_JNDICF               = "com.sun.jndi.ldap.LdapCtxFactory";
    
    private static final StringManager  i18n                         = StringManager.getManager(JdbcAdapter.class);
    
    private Properties                  propsLdap                    = new Properties();
    private String                      defaultBaseDn;
    /** pairs from url and baseDn: acme.com.br ->  dc=acme,dc=com,dc=br */
    private Map<String, String>         urlDc;
    
    private boolean                     sslEnable;
    private boolean forceAuthLdap;
    private Map<String, Vector<String>> cacheGroup;
    
    public LdapAdapter(Properties props) throws BadRealmException, NoSuchRealmException
    {
        this.sslEnable = false; // FIXME configure ssl
        this.urlDc = new HashMap<String, String>();
        this.cacheGroup = new HashMap<String, Vector<String>>();
        setPropertyValue(PROP_DIRURL, "", props);
        setPropertyValue(PROP_DEFAULT_DOMAIN, "", props);
        
        String ctxF = setPropertyValue(PROP_JNDICF, DEFAULT_JNDICF, props);
        this.propsLdap.setProperty(Context.INITIAL_CONTEXT_FACTORY, ctxF);
        String authSec = setPropertyValue(PROP_SECURITY_AUTHENTICATION, DEFAULT_AUTH, props);
        this.propsLdap.setProperty(Context.SECURITY_AUTHENTICATION, authSec);
        this.forceAuthLdap = Boolean.valueOf(props.getProperty(PROP_FORCE_AUTH_LDAP, "false"));
        setPropertyValue(Context.REFERRAL, DEFAULT_REFERRAL, props);
        settingLdapProperties(props);
        // using search filters
        String filter = props.getProperty(PROP_SEARCH_FILTER);
        
        if (filter == null)
            filter = DEFAULT_SEARCH_FILTER;
        else
            filter = filter + "=" + SUBST_SUBJECT_NAME;
        
        setPropertyValue(PROP_SEARCH_FILTER, filter);
        setPropertyValue(PROP_ATTR_GROUP_MEMBER, DEFAULT_FETCH_ATTR, props);
        
        buildDomainComponent();
        checkMandatoryProperties();
    }
    
    public boolean authenticate(final String username, final String password, boolean fetchGroups) throws LoginException
    {
        DirContext ctx = null;
        String userWithDomain = getUserWithDomain(username);
        boolean auth = false;
        if (forceAuthLdap)
        {
            LOG.info(i18n.getString("hybrid.ldap.forcelogin", userWithDomain));
            return true;
        }
        try
        {
            Properties env = getLdapBindProps();
            env.put(Context.SECURITY_PRINCIPAL, userWithDomain);
            env.put(Context.SECURITY_CREDENTIALS, password);
            env.put(Context.PROVIDER_URL, getProviderUrl(userWithDomain));
            ctx = new InitialDirContext(env);
            auth = true;
        }
        catch (NamingException ex)
        {
            String msg = i18n.getString("hybrid.realm.invaliduser", username);
            LOG.log(Level.WARNING, msg);
            if (LOG.isLoggable(Level.FINE))
                LOG.log(Level.FINE, i18n.getString("hybrid.realm.invaliduserpass", username, "***"), ex);
            
            //throw new LoginException(msg + " [" + ex.getMessage() + "]");
        }
        
        if (fetchGroups && ctx != null)
        {
            List<String> groups = getGroupNames(ctx, username);
            Vector<String> groupVector = this.cacheGroup.get(userWithDomain);
            if (groupVector == null)
                groupVector = new Vector<String>();
            for (String group : groups)
            {
                if (!groupVector.contains(group))
                    groupVector.add(group);
            }
            synchronized (this)
            {
                cacheGroup.put(userWithDomain, groupVector);
            }
        }
        try
        {
            if (ctx != null)
                ctx.close();
        }
        catch (NamingException ex)
        {
            LOG.log(Level.WARNING, "cannot close ");
        }
        return auth;
    }
    
    public List<String> getGroupNames(final String username)
    {
        String userWithDomain = getUserWithDomain(username);
        Vector<String> groupVector = this.cacheGroup.get(userWithDomain);
        return (groupVector != null ? groupVector : new Vector<String>());
    }
    
    /**
     * get the user groups in LDAP
     * @param ctx LDAP context
     * @param userWithDomain user name with a domain, ex: user@acme.com.br
     * @return return the list of group names
     */
    private List<String> getGroupNames(DirContext ctx, String userWithDomain)
    {
        List<String> groups = Collections.emptyList();
        // ignore attribute name case
        String filter = this.propsLdap.getProperty(PROP_SEARCH_FILTER);
        filter = String.format(filter, userWithDomain);
        SearchControls ctls = new SearchControls();
        ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        ctls.setCountLimit(1);
        String domain = getDomain(userWithDomain);
        String baseDn = this.urlDc.get(domain);
        LOG.info("base dn -> " + baseDn);
        try
        {
            NamingEnumeration answer = ctx.search(baseDn, filter, ctls);
            if (answer.hasMore())
            {
                SearchResult sr = (SearchResult) answer.next();
                groups = extractGroups(sr.getAttributes());
            }
        }
        catch (NamingException e)
        {
            LOG.log(Level.WARNING, i18n.getString("hybrid.ldap.groupsearcherror", userWithDomain), e);
        }
        finally
        {
            if (ctx != null)
            {
                try
                {
                    ctx.close();
                }
                catch (NamingException e)
                {
                }
            }
        }
        return groups;
    }
    
    private String getDomain(String username)
    {
        String userdomain = getUserWithDomain(username);
        String domain = this.propsLdap.getProperty(PROP_DEFAULT_DOMAIN);
        int i = userdomain.indexOf("@");
        
        if (i > 0)
            domain = userdomain.substring(i + 1);
        
        LOG.finest("domain=" + domain);
        return domain;
    }
    
    /**
     * Build the pairs {@code url} and {@code domain component}.
     * @param props realm properties
     * @throws BadRealmException when malformed URL it's found. 
     */
    private void buildDomainComponent() throws BadRealmException
    {
        String urls = propsLdap.getProperty(PROP_DIRURL);
        String defaultDomain = propsLdap.getProperty(PROP_DEFAULT_DOMAIN);
        
        String[] directories = splitUrl(urls);
        for (String url : directories)
            urlDc.put(url, this.domainComponent(url));
        
        if (directories.length == 0 && defaultDomain != null)
            urlDc.put(defaultDomain, this.domainComponent(defaultDomain));
        
        LOG.finest("build domain=" + urlDc);
    }
    
    private void checkMandatoryProperties() throws BadRealmException
    {
        String url = this.propsLdap.getProperty(PROP_DIRURL);
        String propGroupAttr = this.propsLdap.getProperty(PROP_ATTR_GROUP_MEMBER);
        
        if (url == null || urlDc.isEmpty() || propGroupAttr == null)
            throw new BadRealmException(
                    i18n.getString("hybrid.ldap.badconfig", url, (urlDc.isEmpty() ? "null" : urlDc), propGroupAttr));
    }
    
    private String getProviderUrl(String username)
    {
        String url = getDomain(username);
        boolean hasPort = (url.indexOf(":") > 0);
        String port = (hasPort ? "" : ":" + PORT); //  ldap://acme.com.br:389
        if (sslEnable() && !hasPort)
            port = ":" + PORT_SSL; //  ldaps://acme.com.br:636
            
        if (url.startsWith(URL_LDAP) || url.startsWith(URL_LDAPS))
            url = url + port;
        else
        {
            if (sslEnable())
                url = URL_LDAPS + url + port;
            else
                url = URL_LDAP + url + port;
        }
        LOG.finest("provider url=" + url);
        return url;
    }
    
    private List<String> extractGroups(Attributes attrs) throws NamingException
    {
        List<String> groups = new ArrayList<String>();
        List<String> attrIDs = Arrays.asList(propsLdap.get(PROP_ATTR_GROUP_MEMBER).toString().split(","));
        for (NamingEnumeration ae = attrs.getAll(); ae.hasMore();)
        {
            Attribute attr = (Attribute) ae.next();
            if (attrIDs.contains(attr.getID()))
            {
                LOG.finest("attribute: " + attr.getID());
                NamingEnumeration e = attr.getAll();
                while (e.hasMore())
                {
                    String group = null;
                    String attrValue = String.valueOf(e.next());
                    Matcher matcherCN = PATTERN_CN.matcher(attrValue);
                    if (matcherCN.find())
                    {
                        group = matcherCN.group().substring(3);
                        groups.add(group);
                    }
                    LOG.finest("attr: " + attrValue + ", extract common name as group: " + group);
                }
            }
        }
        return groups;
    }
    
    private String getUserWithDomain(final String username)
    {
        String userdomain = username;
        int at = username.indexOf("@");
        String defaultDomain = this.propsLdap.getProperty(PROP_DEFAULT_DOMAIN);
        
        if (at < 0 && defaultDomain != null && !"".equals(defaultDomain.trim()))
            userdomain = username + "@" + defaultDomain;
        
        LOG.finest("user domain=" + userdomain);
        return userdomain;
    }
    
    private String[] splitUrl(String urls) throws BadRealmException
    {
        if (urls == null)
            return new String[0];
        
        String[] directories = urls.split(",");
        
        for (int i = 0; i < directories.length; i++)
        {
            try
            {
                new URL("http://" + directories[i]);
            }
            catch (MalformedURLException e)
            {
                throw new BadRealmException(e.getMessage());
            }
            directories[i] = directories[i].trim();
        }
        return directories;
    }
    
    private String domainComponent(String url)
    {
        String dc = "";
        int index = url.length();
        int colon = url.indexOf(":");
        if (colon > 0)
            index = colon;
        
        dc = "dc=" + url.substring(0, index).replaceAll("\\.", ",dc=");
        return dc;
    }
    
    private synchronized String setPropertyValue(final String key, final String defaultValue, Properties props)
    {
        String value = props.getProperty(key, defaultValue);
        propsLdap.setProperty(key, value);
        return value;
    }
    
    private synchronized void setPropertyValue(final String key, final String value)
    {
        propsLdap.setProperty(key, value);
    }
    
    private void settingLdapProperties(Properties gfProps)
    {
        for (Map.Entry<Object, Object> e : gfProps.entrySet())
        {
            String key = (String) e.getKey();
            if (key.startsWith("com.sun.jndi."))
            {
                String value = (String) e.getValue();
                setPropertyValue(key, value);
            }
        }
    }
    
    private boolean sslEnable()
    {
        return sslEnable;
    }
    
    /**
     * Get binding properties defined in server.xml for LDAP server.
     *
     */
    private Properties getLdapBindProps()
    {
        return (Properties) propsLdap.clone();
    }
}
