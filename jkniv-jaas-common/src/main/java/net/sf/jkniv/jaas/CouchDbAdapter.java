package net.sf.jkniv.jaas;

import java.nio.charset.Charset;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.login.LoginException;

public class CouchDbAdapter
{

    private static final Logger          LOG                              = MyLoggerFactory.getLogger(CouchDbAdapter.class);

    
    //public static final String           PROP_DATASOURCE_JNDI             = "datasource-jndi";
//    
//    public static final String           PROP_TABLE_USER                  = "user-table";
//    public static final String           PROP_TABLE_USER_COLUMN_NAME      = "user-name-column";
//    
//    public static final String           PROP_TABLE_GROUP                 = "group-table";
//    public static final String           PROP_TABLE_GROUP_COLUMN_USERNAME = "group-table-user-name-column";
//    
//    public static final String           PROP_TABLE_GROUP_COLUMN_NAME     = "group-name-column";
    
    public static final String           PROP_CIPHER_PASSWD               = "cipher-algorithm";
    
    public static final String           PROP_CHARSET                     = "charset";
    
    public static final String           PROP_URL = "url";
    public static final String           PROP_USER = "user";
    public static final String           PROP_PASSWD = "password";
    public static final String           PROP_TABLE_USER_COLUMN_PASSWD    = "user-password-column";
    public static final String           PROP_TABLE_USER_COLUMN_SALT    = "salt-column";
    
//    public static final String           PROP_SQL_GROUP                   = "sql-group";
//    public static final String           PROP_SQL_PASSWORD                = "sql-password";
//    public static final String           PROP_SQL_FOR_SUCCEEDED           = "sql-succeeded";
//    public static final String           PROP_SQL_FOR_FAILED              = "sql-failed";
//    public static final String           PROP_PLACEHOLDER_FOR_EQUAL       = "placeholder-for-equal";
//    /** Place holder for = sql, default is # */
//    private String          placeHolderForEqual ;
    
//    private String                       sqlGroup                         = null;
//    private String                       sqlPasswd                        = null;
//    private String                       sqlForSucceeded                  = null;
//    private String                       sqlForFailed                     = null;
    //private final String                 dsJndi;
    private Cipher                       cipher;
    private CouchDbAuthenticate conn;
    private String url;
    private String baseUrl;
    //private String schema;
    private String user;
    private String passwd;
    private String passwdField;
    private String saltField;
    private Pattern patternPasswd;
    private Pattern patternSalt;
    
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

        this.patternPasswd = Pattern.compile("(?:\""+passwdField+"\":\")(.*?)(?:\")");
        if(cipher.hasSalt())
        {
            this.saltField = props.getProperty(PROP_TABLE_USER_COLUMN_SALT, "salt");
            this.patternSalt = Pattern.compile("(?:\""+saltField+"\":\")(.*?)(?:\")");
        }
        
        conn = new CouchDbAuthenticate(baseUrl, user, passwd);
        LOG.info("COUCHDB Adapter Properties");
        LOG.info("url=" + url +
                 ", user=" + user +
                 ", cipher="+cipher.getAlgorithm() + 
                 ", " + PROP_TABLE_USER_COLUMN_PASSWD + "=" + passwdField + 
                 ", " + PROP_TABLE_USER_COLUMN_SALT + "=" + saltField+
                 ", charset="+charset);
    }

    
    /**
     * Invoke jdbc authenticator.
     *
     * @param username User name to authenticate.
     * @param plainPassword password for authenticate.
     * @returns {{@code true} when the authentication is successfully, {@code false} otherwise.
     */
    public boolean authenticate(String username, String plainPassword) throws LoginException
    {
        boolean auth = false;
        String cookie = conn.getCookieSession();
        if (conn.isExpired())
            cookie = conn.authenticate();
            
        HttpRequest request = new HttpRequest(this.url+"/"+username);
        
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

    
    private String extractUrlFromSchema()
    {
        int ch = this.url.lastIndexOf("/");
        String baseUrl = url.substring(0, ch);
        return baseUrl;
    }

    private String extractJsonData(Matcher matcher)
    {
        String value = "";
        if (matcher.find())
            value = matcher.group().split(":")[1].replaceAll("\"","");
        
        return value;
    }
}
