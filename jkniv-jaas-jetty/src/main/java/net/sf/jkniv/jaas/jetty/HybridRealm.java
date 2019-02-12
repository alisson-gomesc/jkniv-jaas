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

package net.sf.jkniv.jaas.jetty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import javax.security.auth.login.LoginException;

import org.eclipse.jetty.util.log.Logger;
//
//import com.sun.appserv.security.AppservRealm;
//import com.sun.enterprise.security.auth.realm.IASRealm;
//import com.sun.enterprise.security.auth.realm.InvalidOperationException;
//import com.sun.enterprise.security.auth.realm.NoSuchRealmException;
//import com.sun.enterprise.security.auth.realm.NoSuchUserException;
//import com.sun.enterprise.util.i18n.StringManager;

import net.sf.jkniv.jaas.CouchDbAdapter;
import net.sf.jkniv.jaas.I18nManager;
import net.sf.jkniv.jaas.JdbcAdapter;
import net.sf.jkniv.jaas.LdapAdapter;

public class HybridRealm //extends AppservRealm
{
    private static final Logger        LOG                  = MyLoggerFactory.getLogger(HybridRealm.class);
    
    //private static final String        DEFAULT_JAAS_CONTEXT = "hybrid-jaas-context";
    public static final String         PROP_AUTH_TYPE_JDBC  = "authe-jdbc";
    public static final String         PROP_AUTH_TYPE_LDAP  = "authe-ldap";
    public static final String         PROP_AUTH_TYPE_COUCHDB = "authe-couchdb";
    public static final String         PROP_AUTHO_TYPE_JDBC = "autho-jdbc";
    public static final String         PROP_AUTHO_TYPE_LDAP = "autho-ldap";
    public static final String         PROP_AUTHO_TYPE_COUCHDB = "autho-couchdb";
    public static final String         PROP_ASSIGN_GROUPS   = "assign-groups";
    public static final String         PROP_AUTH_TYPE       = "hybrid+ldap+jdbc";
    private JdbcAdapter                jdbcAdapter;
    private LdapAdapter                ldapAdapter;
    private CouchDbAdapter             couchDbAdapter;
    private boolean                    supportsAuthLdap;
    private boolean                    supportsAuthoLdap;
    private boolean                    supportsAuthoCouch;
    private boolean                    supportsAuthJdbc;
    private boolean                    supportsAuthoJdbc;
    private boolean                    supportsAuthCouch;
    private Map<String, Vector>        cacheGroup;
    private Vector<String>             emptyVector;
    private Properties props;
    
    public HybridRealm(Properties props) //throws BadRealmException, NoSuchRealmException
    {
        LOG.info("Starting " + getClass().getSimpleName() + " realm");
        this.cacheGroup = new HashMap<String, Vector>();
        this.emptyVector = new Vector<String>();
        this.props = props;
        System.out.println("new JdbcAdapter...");
        this.jdbcAdapter = new JdbcAdapter(props);
        this.ldapAdapter = new LdapAdapter(props);
        this.couchDbAdapter = new CouchDbAdapter(props);
        if (LOG.isDebugEnabled())
        {
            for (Object k : props.keySet())
                LOG.debug(k + "=" + props.get(k));
        }
        // Pass the properties declared in the console to the system
        //String ctxParam = props.getProperty(IASRealm.JAAS_CONTEXT_PARAM);
        //setProperty(IASRealm.JAAS_CONTEXT_PARAM, ctxParam);
        String assignGroups = props.getProperty(PROP_ASSIGN_GROUPS);
        //if (assignGroups != null && !"".equals(assignGroups.trim()))
        //    setProperty(PROP_ASSIGN_GROUPS, assignGroups);
        
        this.supportsAuthLdap = Boolean.valueOf(props.getProperty(PROP_AUTH_TYPE_LDAP, "true"));
        this.supportsAuthJdbc = Boolean.valueOf(props.getProperty(PROP_AUTH_TYPE_JDBC, "false"));
        this.supportsAuthCouch = Boolean.valueOf(props.getProperty(PROP_AUTH_TYPE_COUCHDB, "false"));
        this.supportsAuthoLdap = Boolean.valueOf(props.getProperty(PROP_AUTHO_TYPE_LDAP, "false"));
        this.supportsAuthoJdbc = Boolean.valueOf(props.getProperty(PROP_AUTHO_TYPE_JDBC, "true"));
        this.supportsAuthoCouch = Boolean.valueOf(props.getProperty(PROP_AUTHO_TYPE_COUCHDB, "false"));
    }
    
    //public String[] authenticate(String username) throws LoginException
    public String[] authenticate(String username, String password) throws LoginException
    {
        String groups[] = null;
        //String password = String.valueOf(_password);
        boolean authLdap = false;
        boolean authJdbc = false;
        boolean authCouch = false;
        
        //        LOG.log(Level.FINEST,"LEVEL FINEST");
        //        LOG.log(Level.FINER,"LEVEL FINER");
        //        LOG.log(Level.FINE,"LEVEL FINE");
        //        LOG.log(Level.INFO,"LEVEL INFO");
        //        LOG.log(Level.CONFIG,"LEVEL CONFIG");
        //        LOG.log(Level.WARNING,"LEVEL WARNING");
        //        LOG.log(Level.SEVERE,"LEVEL SEVERE");
        
        LOG.info(
                I18nManager.getString("hybrid.realm.infoauth", 
                        username + (password==null? ":null" : ":"+password.replaceAll(".", "*")), 
                        Boolean.valueOf(supportsAuthLdap),
                        Boolean.valueOf(supportsAuthJdbc), 
                        Boolean.valueOf(supportsAuthCouch), 
                        Boolean.valueOf(supportsAuthoLdap),
                        Boolean.valueOf(supportsAuthoJdbc), 
                        Boolean.valueOf(supportsAuthoCouch)));        
        
        if (!supportsAuthJdbc && !supportsAuthLdap && !supportsAuthCouch)
            throw new LoginException(I18nManager.getString("hybrid.realm.withoutauth"));
        
//        if (!supportsAuthJdbc && !supportsAuthLdap)
//            throw new LoginException(I18nManager.getString("hybrid.realm.withoutauth"));
        
        if (supportsAuthLdap)
            authLdap = ldapAdapter.authenticate(username, password, supportsAuthoLdap);
        
        if (supportsAuthJdbc)// && !authLdap)
            authJdbc = jdbcAdapter.authenticate(username, password);
        
        if (supportsAuthCouch)// && !authLdap && !authJdbc)
            authCouch = couchDbAdapter.authenticate(username, password);
        
        if (!authLdap && !authJdbc && !authCouch)
        {
            jdbcAdapter.logForFailed(username);
            throw new LoginException(I18nManager.getString("hybrid.realm.loginfail", username));
        }
        
        List<String> grpList = getGroupsFromAdapters(username);
        
        groups = new String[grpList.size()];
        int i = 0;
        for (String g : grpList)
        {
            LOG.info("group -> " + g);
            groups[i++] = g;
        }
        
        jdbcAdapter.logForSucceeded(username);
        return groups;
    }
    
    //@Override
    @SuppressWarnings("unchecked")
    public Enumeration<?> getGroupNames(String username) //throws InvalidOperationException, NoSuchUserException
    {
        Vector<String> vector = this.cacheGroup.get(username);
        if (vector == null)
        {
            List<String> allGroups = getGroupsFromAdapters(username);
            vector = new Vector<String>(allGroups.size());
            for (String g : allGroups)
                vector.addElement(g);
            
            cachingGroupNames(username, allGroups);
            vector = this.cacheGroup.get(username);
        }
        return vector.elements();
    }
    
    private List<String> getGroupsFromAdapters(String username)
    {
        List<String> groupsLdap = Collections.emptyList();
        List<String> groupsJdbc = Collections.emptyList();
        List<String> groupsCouchDb = Collections.emptyList();
        
        if (supportsAuthoLdap)
            groupsLdap = ldapAdapter.getGroupNames(username);
        
        if (supportsAuthoJdbc)
            groupsJdbc = jdbcAdapter.getGroupNames(username);
        
        if (supportsAuthoCouch)
            groupsCouchDb = couchDbAdapter.getGroupNames(username);

        List<String> allGroups = new ArrayList<String>(groupsJdbc.size() + groupsLdap.size());
        allGroups.addAll(groupsLdap);
        allGroups.addAll(groupsJdbc);
        allGroups.addAll(groupsCouchDb);
        
        String assignGroups = this.props.getProperty(PROP_ASSIGN_GROUPS);
        if (assignGroups != null)
        {
            String[] groups = assignGroups.split(",");
            for (String g : groups)
                allGroups.add(g);
        }
        return allGroups;
    }
    
    private Enumeration<String> getGroupsLdap(String username, String password) throws LoginException
    {
        Enumeration<String> groupsLdap = new Hashtable<String, String>().elements();
        //FIXME get groups from LDAP
        return groupsLdap;
    }
    
    private void cachingGroupNames(String username, List<String> groups)
    {
        Vector<String> v = null;
        
        if (groups == null)
        {
            v = emptyVector;
            
        }
        else
        {
            v = new Vector<String>(groups.size());
            for (String g : groups)
            {
                v.add(g);
            }
        }
        
        synchronized (this)
        {
            this.cacheGroup.put(username, v);
        }
    }
    
}
