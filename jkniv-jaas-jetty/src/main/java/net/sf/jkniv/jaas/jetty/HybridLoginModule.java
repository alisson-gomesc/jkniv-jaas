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

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

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
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.security.Credential;

import net.sf.jkniv.jaas.I18nManager;

public class HybridLoginModule extends AbstractLoginModule
{
    private static final Logger    LOG                  = MyLoggerFactory.getLogger(HybridLoginModule.class);
    private HybridRealm _currentRealm;
    private UserInfo userInfo;

    
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
        LOG.info(I18nManager.getString("hybrid.realm.init"));
        Properties propsRealm = new Properties();
        propsRealm.putAll(options);
        this._currentRealm = new HybridRealm(propsRealm);
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
        LOG.info("login user..");
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
                LOG.info(I18nManager.getString("hybrid.realm.loginfail", webUserName));
                setAuthenticated(false);
                throw new FailedLoginException();
            }

            String[] grpList = _currentRealm.authenticate(webUserName, webCredential.toString());
            
            userInfo = new UserInfo(webUserName, Credential.getCredential(webCredential.toString()), Arrays.asList(grpList));

            JAASUserInfo currentUser = new JAASUserInfo(userInfo);
            setCurrentUser(currentUser);
            setAuthenticated(true);
            //currentUser.setJAASInfo(getSubject());
            currentUser.fetchRoles();
//            Method method = currentUser.getClass().getMethod("fetchRoles");
//            if (method != null)
//            {
//                method.invoke(currentUser);
//            }
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
            throw new LoginException (e.toString());
        }
        return true;
    }
    
    @Override
    public UserInfo getUserInfo(String _username) throws Exception
    {
        return userInfo;
        /*
        String[] grpList = null;
        if (!(_currentRealm instanceof HybridRealm))
            throw new LoginException(I18nManager.getString("hybrid.jdbc.badrealm"));
        
        //HybridRealm realm = (HybridRealm) _currentRealm;
        
        grpList = _currentRealm.authenticate(_username, "");
        
        //if (LOG.isLoggable(Level.FINER))
        LOG.info("Hybrid login succeeded for: " + _username + " groups:" + Arrays.toString(grpList));
        UserInfo userInfo = new UserInfo(_username, Credential.getCredential("123456"), Arrays.asList(grpList));
        userInfo.fetchRoles();
        return new UserInfo(_username, Credential.getCredential("123456"), Arrays.asList(grpList));
        */
    }

}
