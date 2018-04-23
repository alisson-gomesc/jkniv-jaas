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

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.jetty.util.log.Logger;

import javax.security.auth.login.LoginException;
import javax.sql.DataSource;

//import com.sun.enterprise.security.auth.digest.api.Password;
//import com.sun.enterprise.security.auth.realm.BadRealmException;
//import com.sun.enterprise.security.auth.realm.InvalidOperationException;
//import com.sun.enterprise.security.auth.realm.NoSuchRealmException;
//import com.sun.enterprise.security.auth.realm.NoSuchUserException;
//import com.sun.enterprise.util.i18n.StringManager;
//import com.sun.logging.LogDomains;


class JdbcAdapter
{
    private static final Logger          LOG                              = MyLoggerFactory.getLogger(JdbcAdapter.class);
    
    
    public static final String           PROP_DATASOURCE_JNDI             = "datasource-jndi";
    
    public static final String           PROP_TABLE_USER                  = "user-table";
    public static final String           PROP_TABLE_USER_COLUMN_NAME      = "user-name-column";
    public static final String           PROP_TABLE_USER_COLUMN_PASSWD    = "user-password-column";
    
    public static final String           PROP_TABLE_GROUP                 = "group-table";
    public static final String           PROP_TABLE_GROUP_COLUMN_USERNAME = "group-table-user-name-column";
    
    public static final String           PROP_TABLE_GROUP_COLUMN_NAME     = "group-name-column";
    
    public static final String           PROP_CIPHER_PASSWD               = "cipher-algorithm";
    
    public static final String           PROP_CHARSET                     = "charset";
    
    public static final String           PROP_SQL_GROUP                   = "sql-group";
    public static final String           PROP_SQL_PASSWORD                = "sql-password";
    public static final String           PROP_SQL_FOR_SUCCEEDED           = "sql-succeeded";
    public static final String           PROP_SQL_FOR_FAILED              = "sql-failed";
    public static final String           PROP_PLACEHOLDER_FOR_EQUAL       = "placeholder-for-equal";
    /** Place holder for = sql, default is # */
    private String          placeHolderForEqual ;
    
    //private Map<String, Vector<String>>    groupCache;
    //private Vector<String>               emptyVector;
    private String                       sqlGroup                         = null;
    private String                       sqlPasswd                        = null;
    private String                       sqlForSucceeded                  = null;
    private String                       sqlForFailed                     = null;
    private final String                 dsJndi;
    private Cipher                       cipher;
    
    /**
     * Initialize a realm with some properties.  This can be used
     * when instantiating realms from their descriptions.  This
     * method may only be called a single time.  
     *
     * @param props Initialization parameters used by this realm.
     * @exception BadRealmException If the configuration parameters
     *     identify a corrupt realm.
     * @exception NoSuchRealmException If the configuration parameters
     *     specify a realm which doesn't exist.
     */
    public JdbcAdapter(Properties props) throws BadRealmException//, NoSuchRealmException
    {
        //this.groupCache = new HashMap<String, Vector<String>>();
        String columunUserName = props.getProperty(PROP_TABLE_USER_COLUMN_NAME);
        String columnPassword = props.getProperty(PROP_TABLE_USER_COLUMN_PASSWD);
        
        String tableUser = props.getProperty(PROP_TABLE_USER);
        String tableGroup = props.getProperty(PROP_TABLE_GROUP);
        String columnGroupName = props.getProperty(PROP_TABLE_GROUP_COLUMN_NAME);
        String columnGroupUserName = props.getProperty(PROP_TABLE_GROUP_COLUMN_USERNAME);
        if (columnGroupUserName == null)
            columnGroupUserName = columunUserName;
        dsJndi = "jdbc/"+props.getProperty(PROP_DATASOURCE_JNDI);
        String cipherAlgoritm = props.getProperty(PROP_CIPHER_PASSWD);
        String charset = props.getProperty(PROP_CHARSET);
        if (charset == null || "".equals(charset.trim()))
            charset = "UTF-8";
        
        if (Cipher.SHA256.equalsIgnoreCase(cipherAlgoritm))
            cipher = new SHA256(Charset.forName(charset));
        else if (Cipher.MD5.equalsIgnoreCase(cipherAlgoritm))
            cipher = new MD5(Charset.forName(charset));
        else if (Cipher.PLAIN_TEXT.equalsIgnoreCase(cipherAlgoritm))
            cipher = new PlainText(Charset.forName(charset));
        else
            cipher = new SHA256(Charset.forName(charset));
        
        // TODO valid mandatory properties
        if (tableGroup == null)
        {
            String msg = I18nManager.getString("hybrid.jdbc.missingprop", PROP_TABLE_GROUP, "JDBCRealm");
            //throw new BadRealmException(msg);
        }
        
        sqlPasswd = "SELECT " + columnPassword + " FROM " + tableUser + " WHERE " + columunUserName + " = ?";
        sqlGroup = "SELECT " + columnGroupName + " FROM " + tableGroup + " WHERE " + columnGroupUserName + " = ? ";
        sqlForSucceeded = props.getProperty(PROP_SQL_FOR_SUCCEEDED);
        sqlForFailed = props.getProperty(PROP_SQL_FOR_FAILED);
        placeHolderForEqual = props.getProperty(PROP_PLACEHOLDER_FOR_EQUAL, "#");
        String customSqlForGroup = props.getProperty(PROP_SQL_GROUP);
        String customSqlForPassword = props.getProperty(PROP_SQL_PASSWORD);
        if (isNotEmpty(customSqlForGroup))
        {
            customSqlForGroup = customSqlForGroup.replaceAll(placeHolderForEqual, "\\=");
            sqlGroup = customSqlForGroup;
        }
        if (isNotEmpty(customSqlForPassword))
        {
            customSqlForPassword = customSqlForPassword.replaceAll(placeHolderForEqual, "\\=");
            sqlPasswd = customSqlForPassword;
        }
        if (isNotEmpty(sqlForSucceeded))
            sqlForSucceeded = sqlForSucceeded.replaceAll(placeHolderForEqual, "\\=");
        if (isNotEmpty(sqlForFailed))
            sqlForFailed = sqlForFailed.replaceAll(placeHolderForEqual, "\\=");

        LOG.info("JDBC Adapter Properties");
        LOG.info("jndi="+dsJndi);
        LOG.info("sqlPasswd="+sqlPasswd);
        LOG.info("sqlGroup="+sqlGroup);
        LOG.info("sqlForSucceeded="+sqlForSucceeded);
        LOG.info("sqlForFailed="+sqlForFailed);
        LOG.info("cipher="+cipher.getAlgorithm());
        LOG.info("charset="+charset);
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
     * @exception InvalidOperationException thrown if the realm does not
     *     support this operation - e.g. Certificate realm does not support
     *     this operation.
     */
    public List<String> getGroupNames(String username) //throws InvalidOperationException, NoSuchUserException
    {
        List<String> groups = findDbGroups(username);
        return groups;
    }
    
    /**
     * Delegate method for retreiving users groups
     * @param user user's identifier
     * @return array of group key
     */
    private List<String> findDbGroups(String user)
    {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        final List<String> groups = new ArrayList<String>();
        try
        {
            connection = getConnection();
            LOG.info(sqlGroup);
            int nroParams = countParams(sqlGroup);
            statement = connection.prepareStatement(sqlGroup);
            for(int i=0;i<nroParams; i++)
                statement.setString(i+1, user);
            
            rs = statement.executeQuery();
            while (rs.next())
                groups.add(rs.getString(1));
        }
        catch (Exception ex)
        {
            String msg = I18nManager.getString("hybrid.jdbc.grouperror", user);
            LOG.warn(msg);
            if (LOG.isDebugEnabled())
                LOG.debug(msg, ex);
        }
        finally
        {
            close(connection, statement, rs);
        }
        return groups;
    }

    /**
     * 
     * @param sql query
     * @return number of parameters at query
     */
    private int countParams(String sql)
    {
        int params = 0;
        for (int i=0; i<sql.length();i++){
            if (sql.charAt(i) == '?')
                params++;
        }
        return params;
    }

    
    /**
     * Invoke jdbc authenticator.
     *
     * @param username User name to authenticate.
     * @param password password for authenticate.
     * @returns {{@code true} when the authentication is successfully, {@code false} otherwise.
     */
    public boolean authenticate(String username, String password) throws LoginException
    {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        boolean auth = false;
        try
        {
            connection = getConnection();
            LOG.debug(sqlPasswd);
            statement = connection.prepareStatement(sqlPasswd);
            int nroParams = countParams(sqlPasswd);
            for(int i=0; i<nroParams; i++)
                statement.setString(i+1, username);
            
            rs = statement.executeQuery();
            
            if (rs.next())
            {
                final String cipherPasswd = cipher.encode(password);
                final String passwdDb = rs.getString(1);
                if (cipherPasswd.equals(passwdDb))
                    auth = true;
            }
        }
        catch (SQLException ex)
        {
            String msg = I18nManager.getString("hybrid.realm.invaliduser", username);
            LOG.warn(msg);
            if (LOG.isDebugEnabled())
                LOG.debug(I18nManager.getString("hybrid.realm.invaliduserpass", username, "***"), ex);
        }
        catch (UnsupportedEncodingException e)
        {
            LOG.warn(I18nManager.getString("hybrid.jdbc.cypher", username), e);
        }
        finally
        {
            close(connection, statement, rs);
        }
        return auth;
    }
    
    public void logForSucceeded(String user)
    {
        if (isEmpty(dsJndi) || isEmpty(sqlForFailed))
            return;
        
        Connection connection = null;
        PreparedStatement statement = null;
        try
        {
            connection = getConnection();
            LOG.debug(sqlForSucceeded);
            int nroParams = countParams(sqlForSucceeded);
            statement = connection.prepareStatement(sqlForSucceeded);
            for(int i=0;i<nroParams; i++)
                statement.setString(i+1, user);
            
            statement.executeUpdate();
        }
        catch (Exception ex)
        {
            String msg = I18nManager.getString("hybrid.jdbc.sqlerror", "sql-succeeded", user);
            LOG.warn(msg);
            if (LOG.isDebugEnabled())
                LOG.debug(msg, ex);
        }
        finally
        {
            close(connection, statement, null);
        }
    }

    public void logForFailed(String user)
    {
        if (isEmpty(dsJndi) || isEmpty(sqlForFailed))
            return;

        Connection connection = null;
        PreparedStatement statement = null;
        try
        {
            connection = getConnection();
            LOG.debug(sqlForFailed);
            int nroParams = countParams(sqlForFailed);
            statement = connection.prepareStatement(sqlForFailed);
            for(int i=0;i<nroParams; i++)
                statement.setString(i+1, user);
            
            statement.executeUpdate();
        }
        catch (Exception ex)
        {
            String msg = I18nManager.getString("hybrid.jdbc.sqlerror", "sql-failed", user);
            LOG.warn(msg);
            if (LOG.isDebugEnabled())
                LOG.debug(msg, ex);
        }
        finally
        {
            close(connection, statement, null);
        }
    }
    
    private void close(Connection conn, PreparedStatement stmt, ResultSet rs)
    {
        if (rs != null)
        {
            try
            {
                rs.close();
            }
            catch (Exception ex)
            {
            }
        }
        
        if (stmt != null)
        {
            try
            {
                stmt.close();
            }
            catch (Exception ex)
            {
            }
        }
        
        if (conn != null)
        {
            try
            {
                conn.close();
            }
            catch (Exception ex)
            {
            }
        }
    }
    
    /**
     * Return a connection from the properties configured
     * @return a connection
     */
    private Connection getConnection() throws LoginException
    {
        try
        {
            final DataSource dataSource = (DataSource) JndiResources.lookup(dsJndi);
            Connection connection = dataSource.getConnection();
            return connection;
        }
        catch (Exception ex)
        {
            String msg = I18nManager.getString("hybrid.jdbc.cantconnect", dsJndi);
            LoginException loginEx = new LoginException(msg);
            loginEx.initCause(ex);
            throw loginEx;
        }
    }

    private boolean isNotEmpty(String s)
    {
        return (s != null && s.trim().length() > 0);
    }
    private boolean isEmpty(String s)
    {
        return (s == null || s.trim().length() < 1);
    }
}
