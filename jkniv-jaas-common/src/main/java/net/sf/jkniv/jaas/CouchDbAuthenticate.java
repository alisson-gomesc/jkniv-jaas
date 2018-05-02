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

import java.net.HttpURLConnection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.security.auth.login.LoginException;

import net.sf.jkniv.jaas.HttpRequest.Method;

class CouchDbAuthenticate
{
    private static final Logger LOG         = MyLoggerFactory.getLogger(CouchDbAuthenticate.class);
    private static final String AUTH_COOKIE = "Set-Cookie";
    private Date                startSession;
    private long                sessionTimeout;
    private String              url;
    private String              username;
    private String              password;
    private String              cookieSession;
    
    /**
     * Build a new Authentication with 10 minutes to expire after authenticate.
     * @param url Uniform Resource Locator pointing to CouchDb instance
     * @param username couchDb user name
     * @param password couchDb password
     */
    public CouchDbAuthenticate(String url, String username, String password)
    {
        super();
        this.url = url + "/_session";
        this.username = username;
        this.password = password;
        this.sessionTimeout = TimeUnit.MINUTES.toMillis(10L);
    }
    
    /**
     * Retrieve the cookie session for user authenticated
     * @return cookie session number for this user authenticated, default session time it's for 10 minutes
     * @throws LoginException when cannot make login in COUCHDB
     */
    public String authenticate() throws LoginException
    {
        HttpRequest conn = null;
        String cookie = "";
        conn = new HttpRequest(this.url, Method.POST);
        HttpResponse _response = conn.send("name=" + username + "&password=" + password);
        if (_response.getStatus() == HttpURLConnection.HTTP_OK)
        {
            List<String> cookies = _response.getHeader(AUTH_COOKIE);
            for (String c : cookies)
            {
                this.cookieSession = c.split(";")[0];
                if (this.cookieSession != null)
                    break;
            }
            cookie = this.cookieSession;
        }
        else if (_response.getStatus() == 401)
        {
            throw new LoginException("Access denied, unauthorized for user [" + username + "] and url [" + url + "]");
        }
        else
        {
            throw new LoginException("Cannot make login in COUCHDB for user [" + username + "] and url [" + url + "]"
                    + "Http code[" + _response.getStatus() + "] " + _response.getBody());
        }
        return cookie;
    }
    
    public String getCookieSession()
    {
        return cookieSession;
    }
    
    public boolean isExpired()
    {
        if (this.startSession == null)
            return true;
        
        return (new Date().getTime() > (this.startSession.getTime() + sessionTimeout));
    }
    
    public void keepAlive()
    {
        this.startSession = new Date();
    }
}
