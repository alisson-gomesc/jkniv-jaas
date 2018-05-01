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

package net.sf.jkniv.jaas.tomcat;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import net.sf.jkniv.jaas.I18nManager;

//import org.eclipse.jetty.jaas.callback.ObjectCallback;
//import org.eclipse.jetty.jaas.spi.UserInfo;
//import org.eclipse.jetty.util.security.Credential;

public class HybridLoginModule implements LoginModule
{
    private static final Logger    LOG                  = MyLoggerFactory.getLogger(HybridLoginModule.class);
    private HybridRealm currentRealm;
    //private UserInfo userInfo;
    private Subject subject;
    private CallbackHandler callbackHandler;
    private Map<String,?> sharedState;
    private Map<String,?> options;
    private boolean authenticated;
    private boolean commitState = false;
    
    private UserPrincipal currentUser;
    private String[] grpList = null;
    
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
        LOG.info(I18nManager.getString("hybrid.realm.init"));
        this.subject = subject;
        this.callbackHandler= callbackHandler;
        this.sharedState = sharedState;
        this.options = options;
        Properties propsRealm = new Properties();
        propsRealm.putAll(options);
        this.currentRealm = new HybridRealm(propsRealm);
    }

    /**
     * @see javax.security.auth.spi.LoginModule#login()
     * @return true if is authenticated, false otherwise
     * @throws LoginException if unable to login
     */
    @Override
    public boolean login() throws LoginException
    {
        try
        {             
            if (callbackHandler == null)
                throw new LoginException ("Callback handler is null");

            Callback[] callbacks = configureCallbacks();
            callbackHandler.handle(callbacks);

            String webUserName = ((NameCallback)callbacks[0]).getName();
            String webCredential = String.valueOf( ((PasswordCallback)callbacks[1]).getPassword());

            if (webUserName == null || webCredential == null)
            {
                LOG.info(I18nManager.getString("hybrid.realm.loginfail", webUserName));
                setAuthenticated(false);
                throw new FailedLoginException();
            }

            this.grpList = currentRealm.authenticate(webUserName, webCredential);
            this.currentUser = new UserPrincipal(webUserName, webCredential);
            
            //userInfo = new UserInfo(webUserName, Credential.getCredential(webCredential.toString()), Arrays.asList(grpList));

            //JAASUserInfo currentUser = new JAASUserInfo(userInfo);
            //setCurrentUser(currentUser);
            setAuthenticated(true);
            //currentUser.fetchRoles();
            LOG.info(I18nManager.getString("hybrid.realm.login.successfully", webUserName));
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
            LOG.log(Level.SEVERE, e.getMessage(), e);
            throw new LoginException (e.toString());
        }
        return true;
    }
    
    /**
     * @see javax.security.auth.spi.LoginModule#commit()
     * @return true if committed, false if not (likely not authenticated)
     * @throws LoginException if unable to commit
     */
    @Override
    public boolean commit() throws LoginException
    {
        if (!isAuthenticated())
        {
            currentUser = null;
            setCommitted(false);
            return false;
        }

        setCommitted(true);
        subject.getPrincipals().add(this.currentUser);
        subject.getPrivateCredentials().add(this.currentUser.getCredential());
        if (grpList != null)
        {
            for(String g : grpList)
                subject.getPrincipals().add(new RolePrincipal(g));
        }
        return true;
    }
    
    /**
     * @see javax.security.auth.spi.LoginModule#logout()
     * @return true always
     * @throws LoginException if unable to logout
     */
    @Override
    public boolean logout() throws LoginException
    {
        this.unsetJAASInfo();
        this.currentUser = null;
        return true;
    }
    
    
    /**
     * @see javax.security.auth.spi.LoginModule#abort()
     * @throws LoginException if unable to abort
     */
    @Override
    public boolean abort() throws LoginException
    {
        this.currentUser = null;
        return (isAuthenticated() && isCommitted());
    }

    
    private Callback[] configureCallbacks ()
    {
        Callback[] callbacks = new Callback[2];
        callbacks[0] = new NameCallback("Enter user name");
        callbacks[1] = new PasswordCallback("Enter password", false); //only used if framework does not support the ObjectCallback
        return callbacks;
    }

    private void setCommitted (boolean commitState)
    {
        this.commitState = commitState;
    }
    
    private boolean isCommitted ()
    {
        return this.commitState;
    }

    private void setAuthenticated(boolean authenticated)
    {
        this.authenticated = authenticated;
    }

    private boolean isAuthenticated()
    {
        return authenticated;
    }
    
    private void unsetJAASInfo ()
    {
        subject.getPrincipals().remove(this.currentUser);
        subject.getPrivateCredentials().remove(this.currentUser.getCredential());
        if (grpList != null)
        {
            for(String g : grpList)
                subject.getPrincipals().remove(new RolePrincipal(g));
        }
    }

}
