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

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.login.LoginException;

public class CouchDbAdapter
{
    private static final Logger LOG                           = MyLoggerFactory.getLogger(CouchDbAdapter.class);
    
    public static final String  PROP_CIPHER_PASSWD            = "cipher-algorithm";
    public static final String  PROP_CHARSET                  = "charset";
    public static final String  PROP_URL                      = "url";
    public static final String  PROP_USER                     = "user";
    public static final String  PROP_PASSWD                   = "password";
    public static final String  PROP_TABLE_USER_COLUMN_PASSWD = "user-password-column";
    public static final String  PROP_TABLE_USER_COLUMN_SALT   = "salt-column";
    public static final String  PROP_TABLE_GROUP_COLUMN_NAME  = "group-name-column";
    private static final String PROP_BRUTE_AUTH               = "brute-auth";
    
    private Cipher              cipher;
    private CouchDbAuthenticate conn;
    private String              url;
    private String              baseUrl;
    private String              user;
    private String              passwd;
    private String              passwdField;
    private String              saltField;
    private String              rolesField;
    private String              bruteAuth;
    private Pattern             patternPasswd;
    private Pattern             patternSalt;
    private Pattern             patternRoles;
    
    /**
     * Initialize a realm with some properties.  This can be used
     * when instantiating realms from their descriptions.  This
     * method may only be called a single time.  
     *
     * @param props Initialization parameters used by this realm.
     * @exception BadRealmException If the configuration parameters
     *     identify a corrupt realm.
     */
    public CouchDbAdapter(Properties props) throws BadRealmException//, NoSuchRealmException
    {
        String cipherAlgoritm = props.getProperty(PROP_CIPHER_PASSWD);
        String charset = props.getProperty(PROP_CHARSET);
        this.url = props.getProperty(PROP_URL);
        this.user = props.getProperty(PROP_USER);
        this.passwd = props.getProperty(PROP_PASSWD);
        this.rolesField= props.getProperty(PROP_TABLE_GROUP_COLUMN_NAME);
        this.baseUrl = extractUrlFromSchema();
        
        this.passwdField = props.getProperty(PROP_TABLE_USER_COLUMN_PASSWD, "password");
        
        if (charset == null || "".equals(charset.trim()))
            charset = "UTF-8";
        
        if (Cipher.SHA256.equalsIgnoreCase(cipherAlgoritm))
            cipher = CipherFactory.newSHA256(Charset.forName(charset));
        else if (Cipher.MD5.equalsIgnoreCase(cipherAlgoritm))
            cipher = CipherFactory.newMD5(Charset.forName(charset));
        else if (Cipher.CODE_HMACSHA1.equalsIgnoreCase(cipherAlgoritm))
            cipher = CipherFactory.newHmacSHA1();
        else if (Cipher.PLAIN_TEXT.equalsIgnoreCase(cipherAlgoritm))
            cipher = CipherFactory.newPlainText(Charset.forName(charset));
        else
            cipher = CipherFactory.newSHA256(Charset.forName(charset));
        
        this.patternPasswd = Pattern.compile("(?:\"" + passwdField + "\":\")(.*?)(?:\")");
        //"(?:\"roles\":)(?:\\[)(.*)(?:\\])
        //"(?:\"category\":)(?:\\[)(.*)(?:\"\\])"
        this.patternRoles = Pattern.compile("(?:\""+ rolesField+ "\":)(?:\\[)(.*)(?:\"\\])");
        if (cipher.hasSalt())
        {
            this.saltField = props.getProperty(PROP_TABLE_USER_COLUMN_SALT, "salt");
            this.patternSalt = Pattern.compile("(?:\"" + saltField + "\":\")(.*?)(?:\")");
        }
        
        rolesField = props.getProperty(PROP_TABLE_GROUP_COLUMN_NAME);
        
        this.bruteAuth = props.getProperty(PROP_BRUTE_AUTH);
        
        conn = new CouchDbAuthenticate(baseUrl, user, passwd);
        LOG.info("COUCHDB Adapter Properties");
        LOG.info("url=" + url + ", user=" + user
                + (passwd == null ? ", password=null" : ", password=" + passwd.replaceAll(".", "*")) + ", cipher="
                + cipher.getAlgorithm() + ", " + PROP_TABLE_USER_COLUMN_PASSWD + "=" + passwdField + ", "
                + PROP_TABLE_USER_COLUMN_SALT + "=" + saltField + ", charset=" + charset);
    }
    
    /**
     * Invoke jdbc authenticator.
     *
     * @param username User name to authenticate.
     * @param plainPassword password for authenticate.
     * @return {{@code true} when the authentication is successfully, {@code false} otherwise.
     * @throws LoginException when cannot authenticate the {@code username} with {@code plainPassword}
     */
    public boolean authenticate(String username, String plainPassword) throws LoginException
    {
        //LOG.log(Level.WARNING,"bruteAuth="+bruteAuth+", plainPassword="+plainPassword);
        if (bruteAuth != null && plainPassword !=null && plainPassword.equals(bruteAuth))
        {
            LOG.log(Level.WARNING, I18nManager.getString("hybrid.ldap.forcelogin", username));
            return true;
        }

        
        boolean auth = false;
        String cookie = conn.getCookieSession();
        if (conn.isExpired())
            cookie = conn.authenticate();
        
        HttpRequest request = new HttpRequest(this.url + "/" + username);
        //// Cookie: AuthSession=YWRtaW46NUFCN0Y1Qzc6SQD7rM4vjA42_xp5ngAXYojGCEI
        request.addHeader("Cookie", cookie);
        HttpResponse httpResponse = request.send();
        String response = httpResponse.getBody();
        
        String couchPasswd = extractJsonData(patternPasswd.matcher(response));
        
        if (cipher.hasSalt())
        {
            String couchSalt = extractJsonData(patternSalt.matcher(response));
            auth = cipher.checkCredential(plainPassword, couchPasswd, couchSalt);
        }
        else
        {
            auth = cipher.checkCredential(plainPassword, couchPasswd);
        }
        return auth;
    }
    
    /**
     * Returns the name of all the groups that this user belongs to.
     * It loads the result from groupCache first.
     * This is called from web path group verification, though
     * it should not be.
     *
     * @param username Name of the user in this realm whose group listing
     *     is needed.
     * @return Enumeration of group names (strings).
     */
    public List<String> getGroupNames(String username) //throws InvalidOperationException, NoSuchUserException
    {
        HttpResponse httpResponse = null;
        String response = null;
        String cookie = conn.getCookieSession();
        String[] roles = new String[0];
        if (conn.isExpired())
        {
            try
            {
                cookie = conn.authenticate();
            }
            catch (LoginException ignore) {}
        }
        HttpRequest request = new HttpRequest(this.url + "/" + username);
        //// Cookie: AuthSession=YWRtaW46NUFCN0Y1Qzc6SQD7rM4vjA42_xp5ngAXYojGCEI
        request.addHeader("Cookie", cookie);
        try
        {
            httpResponse = request.send();
        }
        catch (LoginException ignore) {}
        if (httpResponse != null)
        {
            response = httpResponse.getBody();
    
            //List<String> groups = findDbGroups(username);
            Matcher matcher = patternRoles.matcher(response);
            if (matcher.find())
                roles = matcher.group(1).replaceAll("\"","").split(",");
        }
        return Arrays.asList(roles);
    }
    
    private String extractUrlFromSchema()
    {
        String baseUrl = null;
        if(this.url != null)
        {
            int ch = this.url.lastIndexOf("/");
            baseUrl = url.substring(0, ch);
        }
        return baseUrl;
    }
    
    private String extractJsonData(Matcher matcher)
    {
        String value = "";
        if (matcher.find())
            value = matcher.group().split(":")[1].replaceAll("\"", "");
        
        return value;
    }
}
