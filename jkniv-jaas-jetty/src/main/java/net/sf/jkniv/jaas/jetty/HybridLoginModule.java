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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
//import java.util.logging.Level;
//import java.util.logging.Logger;

import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.security.Credential;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

import org.eclipse.jetty.jaas.callback.ObjectCallback;
import org.eclipse.jetty.jaas.spi.AbstractLoginModule;
import org.eclipse.jetty.jaas.spi.UserInfo;
import org.eclipse.jetty.jaas.spi.AbstractLoginModule.JAASUserInfo;

public class HybridLoginModule extends AbstractLoginModule
{
    private static final Logger    LOG                  = MyLoggerFactory.getLogger(HybridLoginModule.class);
    private HybridRealm _currentRealm;
    /*
    public static final String     PROP_AUTH_TYPE_JDBC  = "authe-jdbc";
    public static final String     PROP_AUTH_TYPE_LDAP  = "authe-ldap";
    public static final String     PROP_AUTHO_TYPE_JDBC = "autho-jdbc";
    public static final String     PROP_AUTHO_TYPE_LDAP = "autho-ldap";
    public static final String     PROP_ASSIGN_GROUPS   = "assign-groups";
    public static final String     PROP_AUTH_TYPE       = "hybrid+ldap+jdbc";
    
    private boolean                    supportsAuthLdap;
    private boolean                    supportsAuthoLdap;
    private boolean                    supportsAuthJdbc;
    private boolean                    supportsAuthoJdbc;
    
    private JdbcAdapter                jdbcAdapter;
    private LdapAdapter                ldapAdapter;
    private Map<String, Vector>        cacheGroup;
    private Vector<String>             emptyVector;
*/
    /* ------------------------------------------------ */
    /** 
     * Init LoginModule.
     * <p>
     * Called once by JAAS after new instance created.
     * 
     * @param subject the subject
     * @param callbackHandler the callback handler
     * @param sharedState the shared state map
     * @param options the options map
     */
    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState,
            Map<String, ?> options)
    {
        super.initialize(subject, callbackHandler, sharedState, options);
        Properties propsRealm = new Properties();
        propsRealm.putAll(options);
        this._currentRealm = new HybridRealm(propsRealm);
        
        /*
        this.cacheGroup = new HashMap<String, Vector>();
        this.emptyVector = new Vector<String>();
        
        this.jdbcAdapter = new JdbcAdapter(propsRealm);
        this.ldapAdapter = new LdapAdapter(propsRealm);
        LOG.info("Init clsiv Realm");
        if (LOG.isDebugEnabled())
        {
            for (Object k : options.keySet())
                LOG.debug(k + "=" + options.get(k));
        }
        // Pass the properties declared in the console to the system
        //String ctxParam = options.getProperty(IASRealm.JAAS_CONTEXT_PARAM);
        //setProperty(IASRealm.JAAS_CONTEXT_PARAM, ctxParam);
        String assignGroups = propsRealm.getProperty(PROP_ASSIGN_GROUPS);
        
        this.supportsAuthLdap = Boolean.valueOf(propsRealm.getProperty(PROP_AUTH_TYPE_LDAP, "true"));
        this.supportsAuthJdbc = Boolean.valueOf(propsRealm.getProperty(PROP_AUTH_TYPE_JDBC, "false"));
        this.supportsAuthoLdap = Boolean.valueOf(propsRealm.getProperty(PROP_AUTHO_TYPE_LDAP, "false"));
        this.supportsAuthoJdbc = Boolean.valueOf(propsRealm.getProperty(PROP_AUTHO_TYPE_JDBC, "true"));
        */
    }

    /**
     * @see javax.security.auth.spi.LoginModule#login()
     * @return true if is authenticated, false otherwise
     * @throws LoginException if unable to login
     */
    @Override
    public boolean login() throws LoginException
    {
        CallbackHandler callbackHandler = getCallbackHandler();
        try
        {  
            if (isIgnored())
                return false;
            
            if (callbackHandler == null)
                throw new LoginException ("No callback handler");

            Callback[] callbacks = configureCallbacks();
            callbackHandler.handle(callbacks);

            String webUserName = ((NameCallback)callbacks[0]).getName();
            Object webCredential = null;

            webCredential = ((ObjectCallback)callbacks[1]).getObject(); //first check if ObjectCallback has the credential
            if (webCredential == null)
                webCredential = ((PasswordCallback)callbacks[2]).getPassword(); //use standard PasswordCallback

            if ((webUserName == null) || (webCredential == null))
            {
                setAuthenticated(false);
                throw new FailedLoginException();
            }

            String[] grpList = _currentRealm.authenticate(webUserName, webCredential.toString());

            UserInfo userInfo = new UserInfo(webUserName, Credential.getCredential(webCredential.toString()), Arrays.asList(grpList));
            //UserInfo userInfo = getUserInfo(webUserName);

            JAASUserInfo currentUser = new JAASUserInfo(userInfo);
            setCurrentUser(currentUser);
            setAuthenticated(true);
        }
        catch (IOException e)
        {
            throw new LoginException (e.toString());
        }
        catch (UnsupportedCallbackException e)
        {
            throw new LoginException (e.toString());
        }
        catch (Exception e)
        {
            if (e instanceof LoginException)
                throw (LoginException)e;
            throw new LoginException (e.toString());
        }
        return true;
    }

    /*
    @Override
    protected void authenticateUser() throws LoginException
    {
        String[] grpList = null;
        if (!(_currentRealm instanceof HybridRealm))
            throw new LoginException(sm.getString("hybrid.jdbc.badrealm"));
        
        HybridRealm realm = (HybridRealm) _currentRealm;
        
        grpList = realm.authenticate(_username, _passwd);
        
        //if (LOG.isLoggable(Level.FINER))
        LOG.info("Hybrid login succeeded for: " + _username + " groups:" + Arrays.toString(grpList));
        
        // populate grpList with the set of groups to which _username belongs in this realm, if any
        commitUserAuthentication(grpList);
    }
    */
    
    @Override
    public UserInfo getUserInfo(String _username) throws Exception
    {
        String[] grpList = null;
        if (!(_currentRealm instanceof HybridRealm))
            throw new LoginException(I18nManager.getString("hybrid.jdbc.badrealm"));
        
        //HybridRealm realm = (HybridRealm) _currentRealm;
        
        grpList = _currentRealm.authenticate(_username, "");
        
        //if (LOG.isLoggable(Level.FINER))
        LOG.info("Hybrid login succeeded for: " + _username + " groups:" + Arrays.toString(grpList));

        return new UserInfo(_username, Credential.getCredential("123456"), Arrays.asList(grpList));
    }


}
