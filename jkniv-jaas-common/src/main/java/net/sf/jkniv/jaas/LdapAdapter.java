/* 
 * JKNIV JAAS,
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

package net.sf.jkniv.jaas;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.security.auth.login.LoginException;

public class LdapAdapter
{
    private static final Logger          LOG                          = MyLoggerFactory.getLogger(LdapAdapter.class);
    
    // find the pattern CN=...
    private static final String          REGEX_COMMON_NAME            = "CN=[\\w\\.?]+";                             // CN=my_group,OU=dev,OU=acme,DC=acme,DC=com,DC=br   ---> CN=my_group
    public static final Pattern          PATTERN_CN                   = Pattern.compile(REGEX_COMMON_NAME,
            Pattern.CASE_INSENSITIVE);
    
    /** LDAP URL for your server */
    public static final String           PROP_DIRURL                  = "directories";
    
    /* LDAP base DN for the location of user data   */
    //public static final String         PROP_BASEDN                  = "base-dn";
    
    /** security level to use "none", "simple", "strong". */
    public static final String           PROP_SECURITY_AUTHENTICATION = "auth-level";
    
    /** Default domain from users user@mydomain.com */
    public static final String           PROP_DEFAULT_DOMAIN          = "default-domain";
    
    /** Attribute name thats representgroup-member-attr group of user <b>group-member-attr</b> */
    public static final String           PROP_ATTR_GROUP_MEMBER       = "group-member-attr";
    
    public static final String           DEFAULT_AUTH                 = "simple";
    
    public static final String           DEFAULT_FETCH_ATTR           = "memberOf";
    private static final String          DEFAULT_REFERRAL             = "follow";
    
    private static final String          PROP_BRUTE_AUTH              = "brute-auth";
    
    private static final String          DEFAULT_POOL_PROTOCOL        = "plain ssl";
    private static final String          SSL                          = "SSL";
    
    private static final LdapEntryParser LDAP_PARSER                  = new LdapEntryParser();
    // --------------------------------------------------------------------------------------------- //
    
    // These are optional, defaults are provided
    // %s = subject name
    // %d = DN of user search result
    public static final String           PROP_SEARCH_FILTER           = "search-filter";
    public static final String           PROP_JNDICF                  = "jndiCtxFactory";
    public static final String           PROP_READ_TIMEOUT            = "read.timeout";
    // Expansion strings
    public static final String           SUBST_SUBJECT_NAME           = "%s";
    public static final String           SUBST_SUBJECT_DN             = "%d";
    
    // Defaults
    private static final String          DEFAULT_SEARCH_FILTER        = "mail=" + SUBST_SUBJECT_NAME;
    private static final String          DEFAULT_JNDICF               = "com.sun.jndi.ldap.LdapCtxFactory";
    
    private Properties                   propsLdap                    = new Properties();
    private String                       defaultBaseDn;
    /** pairs from url and baseDn: acme.com.br ->  dc=acme,dc=com,dc=br */
    private Map<String, URI>             urlDc;
    
    private String                       bruteAuth;
    private Map<String, Vector<String>>  cacheGroup;
    private LdapConnection               ldapConn;
    
    public LdapAdapter(Properties props) throws BadRealmException//, NoSuchRealmException
    {
        this(props, new LdapConnectionImpl());
    }
    
    public LdapAdapter(Properties props, LdapConnection ldapConn) throws BadRealmException//, NoSuchRealmException
    {
        this.ldapConn = ldapConn;
        this.urlDc = new HashMap<String, URI>();
        this.cacheGroup = new HashMap<String, Vector<String>>();
        setPropertyValue(PROP_DIRURL, "", props);
        setPropertyValue(PROP_DEFAULT_DOMAIN, "", props);
        
        String ctxF = setPropertyValue(PROP_JNDICF, DEFAULT_JNDICF, props);
        this.propsLdap.setProperty(Context.INITIAL_CONTEXT_FACTORY, ctxF);
        String authSec = setPropertyValue(PROP_SECURITY_AUTHENTICATION, DEFAULT_AUTH, props);
        this.propsLdap.setProperty(Context.SECURITY_AUTHENTICATION, authSec);
        this.bruteAuth = props.getProperty(PROP_BRUTE_AUTH);
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
        LOG.info("LDAP Adapter Properties");
        for (Entry<Object, Object> entry : propsLdap.entrySet())
            LOG.info(entry.getKey() + "=" + entry.getValue());
    }
    
    public boolean authenticate(final String username, final String password, boolean fetchGroups) throws LoginException
    {
        DirContext ctx = null;
        String defaultDomain = this.propsLdap.getProperty(PROP_DEFAULT_DOMAIN);
        String userWithDomain = LDAP_PARSER.appendDomain(username, defaultDomain);
        boolean auth = false;
        if (bruteAuth != null && password != null && password.equals(bruteAuth))
        {
            LOG.log(Level.WARNING, I18nManager.getString("hybrid.ldap.forcelogin", userWithDomain));
            return true;
        }
        try
        {
            Properties env = getLdapBindProps();
            env.put(Context.SECURITY_PRINCIPAL, userWithDomain);
            env.put(Context.SECURITY_CREDENTIALS, password);
            env.put(Context.PROVIDER_URL, getProviderUrl(userWithDomain));
            ctx = this.ldapConn.openDir(env);
            auth = true;
        }
        catch (NamingException ex)
        {
            String msg = I18nManager.getString("hybrid.realm.invaliduser", username);
            LOG.log(Level.WARNING, msg);
            LOG.log(Level.FINE, I18nManager.getString("hybrid.realm.invaliduserpass", username, "***"), ex);
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
            LOG.log(Level.WARNING, "cannot close ldap context");
        }
        return auth;
    }
    
    public List<String> getGroupNames(final String username)
    {
        String defaultDomain = this.propsLdap.getProperty(PROP_DEFAULT_DOMAIN);
        String userWithDomain = LDAP_PARSER.appendDomain(username, defaultDomain);
        Vector<String> groupVector = this.cacheGroup.get(userWithDomain);
        return (groupVector != null ? groupVector : new Vector<String>());
    }
    
    /**
     * get the user groups in LDAP
     * @param ctx LDAP context
     * @param userWithDomain user name with a domain, ex: user@acme.com.br
     * @return return the list of group names
     */
    @SuppressWarnings("rawtypes")
    private List<String> getGroupNames(DirContext ctx, String userWithDomain)
    {
        String defaultDomain = this.propsLdap.getProperty(PROP_DEFAULT_DOMAIN);
        List<String> groups = Collections.emptyList();
        // ignore attribute name case
        String filter = this.propsLdap.getProperty(PROP_SEARCH_FILTER);
        filter = String.format(filter, userWithDomain);
        SearchControls ctls = new SearchControls();
        ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        ctls.setCountLimit(1);
        String domain = LDAP_PARSER.stripDomain(userWithDomain, defaultDomain);
        String baseDn = LDAP_PARSER.dcFrom(this.urlDc.get(domain).getHost());
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
            LOG.log(Level.SEVERE, I18nManager.getString("hybrid.ldap.groupsearcherror", userWithDomain) + ", cause: "
                    + e.getMessage());
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
    
    /**
     * Build the pairs {@code url} and {@code domain component}.
     * @param props realm properties
     * @throws BadRealmException when malformed URL it's found. 
     */
    private void buildDomainComponent() throws BadRealmException
    {
        String urls = propsLdap.getProperty(PROP_DIRURL);
        String defaultDomain = propsLdap.getProperty(PROP_DEFAULT_DOMAIN);
        
        URI[] directories = LDAP_PARSER.splitUri(urls);
        for (URI uri : directories)
            urlDc.put(uri.getHost(), uri);
        
        try
        {
            if (directories.length == 0 && defaultDomain != null)
                urlDc.put(defaultDomain, new URI("ldap://" + defaultDomain));
        }
        catch (URISyntaxException e)
        {
            throw new BadRealmException(e.getMessage());
        }
        LOG.log(Level.FINE, "build domain=" + urlDc);
    }
    
    private void checkMandatoryProperties() throws BadRealmException
    {
        String url = this.propsLdap.getProperty(PROP_DIRURL);
        String propGroupAttr = this.propsLdap.getProperty(PROP_ATTR_GROUP_MEMBER);
        
        if (url == null || urlDc.isEmpty() || propGroupAttr == null)
            throw new BadRealmException(I18nManager.getString("hybrid.ldap.badconfig", url,
                    (urlDc.isEmpty() ? "null" : urlDc), propGroupAttr));
    }
    
    /**
     * The value of the property should contain a URL string (e.g. "ldap://somehost:389").
     * @param usernameWithDomain user like algo@somehost.com
     * @return
     */
    private String getProviderUrl(String usernameWithDomain)
    {
        String defaultDomain = this.propsLdap.getProperty(PROP_DEFAULT_DOMAIN);
        String domain = LDAP_PARSER.stripDomain(usernameWithDomain, defaultDomain);
        domain = this.urlDc.get(domain).toString();
        /*
        boolean hasPort = (domain.indexOf(":") > 0);
        String port = (hasPort ? "" : ":" + PORT); //  ldap://acme.com.br:389
        if (sslEnable() && !hasPort)
            port = ":" + PORT_SSL; //  ldaps://acme.com.br:636
            
        if (domain.startsWith(URL_LDAP) || domain.startsWith(URL_LDAPS))
            domain = domain + port;
        else
        {
            if (sslEnable())
                domain = URL_LDAPS + domain + port;
            else
                domain = URL_LDAP + domain + port;
        }
        */
        LOG.log(Level.FINE, "provider url=" + domain);
        return domain;
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
                LOG.log(Level.FINE, "attribute: " + attr.getID());
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
                    LOG.log(Level.FINE, "attr: " + attrValue + ", extract common name as group: " + group);
                }
            }
        }
        return groups;
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
    
    /**
     * Get binding properties defined in server.xml for LDAP server.
     *
     */
    private Properties getLdapBindProps()
    {
        return (Properties) propsLdap.clone();
    }
}
