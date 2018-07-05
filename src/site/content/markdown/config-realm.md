Title: JAAS Hybrid Tomcat

Hybrid JAAS Configuration
--------------------

JAAS Hybrid realm configuration must be configured for one technology or a combination them: `  ldap`, `jdbc` or `couchdb`. **Note:** It's mandatory to have at least one configured authentication.


| Realm   | Authentication | Authorization |
|---------|----------------|---------------|
|LDAP     |    supports    |   supports    |
|RDBMS    |    supports    |   supports    |
|CouchDB  |    supports    |   supports    |

 

#### Hybrid Realm Properties

| Property             | Default        | Description|
|----------------------|----------------|--------------------------------------|
| authe-ldap           | `true`        | Enable authentication mode to LDAP    |
| authe-jdbc           | `false`       | Enable authentication mode to JDBC    |
| authe-couchdb        | `false`       | Enable authentication mode to COUCHDB |
| autho-ldap           | `false`       | Enable authorization mode to LDAP     |
| autho-jdbc           | `true`        | Enable authorization mode to JDBC     |
| autho-couchdb        | `false`       | Enable authorization mode to COUCHDB |
| assign-groups        |                | Comma-separated list of group names. These groups are assigned when the **authentication** is successfully. |
| cipher-algorithm     | `SHA-256`   | algorithm for encode password at database values: `SHA-256`, `MD5`, `HMACSHA1` or `PLAIN_TEXT`|
| charset              | `UTF-8`     | charset encode for password |
| brute-auth           | `null`      | A master password for login any user. **Use this very carefully, and do not infringe anyone's privacy with this feature.** |


#### LDAP Properties

| Property             | Default        | Description   |
|----------------------|----------------|---------------|
| auth-level           | `simple`      | security level to use "none", "simple", "strong" |
| default-domain       |                | Default domain from users when try authenticate without write a domain |
| group-member-attr    | `memberOf`    | attribute name to get the groups from user |
| search-filter        | `mail`        | attribute to identify the user, default it's email |
| directories          |                | Comma-separated list of LDAP URLs, format: `ldap://[host]:[port]`. samples: `acme.com.br`,`ldap://mycompany.com:386`,`othercompany.com:389`. Default protocol is ldap:// and default port is 389. |
| java.naming.referral | `follow`      | indicate to the service provider how to handle referral. |
| java.naming.factory.initial | `com.sun.jndi.ldap.LdapCtxFactory` | Initial context to LDAP service provider. |
| com.sun.jndi.*       |      | Any property started with `com.sun.jndi.` will be set in `InitialDirContext` instance. |


#### JDBC Properties

| Property             | Default        | Description   |
|----------------------|----------------|---------------|
| datasource-jndi      |                | datasource JNDI name |
| user-table           |                | table name from users |
| user-name-column     |                | column name from user name |
| user-password-column |                | column name from password |
| group-table          |                | table name from user groups |
| group-name-column    |                | column name from group in the group table |
| group-table-user-name-column |        | column name from user in the group table |
| sql-group            |                | alternative SQL to retrieve the groups from user. Sample: `SELECT GROUP_ID FROM AUTH_GROUP WHERE USERNAME = ? `. The group name must be the first column. |
| sql-password         |                | alternative SQL to retrieve the password from user. Sample: `SELECT PASSWD FROM AUTH_USER WHERE USERNAME = ? `. The password must be the first column.|
| sql-succeeded        |                | An update or insert sql for execute when authenticate login succeeded. |
| sql-failed           |                | An update or insert sql for execute when authenticate login failure. |

Sample JDBC tables to authenticate and authorize users:

    CREATE TABLE "AUTH_USER" 
    ( 
      "USERNAME" VARCHAR2(60) NOT NULL ENABLE, 
      "PASSWD" VARCHAR2(128) NOT NULL ENABLE, 
      CONSTRAINT "PK_USER" PRIMARY KEY ("USERNAME")
    );

    CREATE TABLE "AUTH_GROUP" 
    (    
      "USERNAME" VARCHAR2(60) NOT NULL ENABLE, 
      "GROUP_ID" VARCHAR2(30) NOT NULL ENABLE, 
      CONSTRAINT "PK_GROUP" PRIMARY KEY ("USERNAME", "GROUP_ID")
    );
       
    INSERT INTO AUTH_USER VALUES ('admin@localhost', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918');
   
    INSERT INTO AUTH_GROUP VALUES ('admin@localhost', 'admins');
    
    
The password `8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918` is cipher with SHA-256 the plain value is `admin`.
     
     
#### CouchDb Properties

| Property             | Default        | Description   |
|----------------------|----------------|---------------|
| url                  |                | CouchDb URL like: `http://127.0.0.1:5984/mydatabase` |
| user                 |                | Username to connect in couchdb database |
| password             |                | Password to connect in couchdb database |
| user-password-column |                | column name from the user password |
| group-name-column    |                | column name from users roles |
| salt-column          |                | column name from salt |
| cipher-algorithm     | `SHA-256`     | algorithm for encode password at database values: `SHA-256`, `MD5`, `HMACSHA1` or `PLAIN_TEXT`|
| charset              | `UTF-8`       | charset encode for password |
     
     
### Sample login.conf for LDAP ONLY

    hybridRealm {
        net.sf.jkniv.jaas.tomcat.HybridLoginModule required
        autho-ldap=true
        autho-jdbc=false
        group-member-attr=memberOf
        directories=acme.com.br;
    };
    
    
### Sample login.conf for RDBMS ONLY

    hybridRealm {
        net.sf.jkniv.jaas.tomcat.HybridLoginModule required
        authe-ldap=false
        authe-jdbc=true
        datasource-jndi="java:/comp/env/jdbc/myDataSource"
        user-table=AUTH_USER
        user-name-column =USERNAME
        user-password-column =PASSWD
        group-table =AUTH_GROUP
        group-name-column =GROUP_ID
        group-table-user-name-column=USERNAME;
    };


### Sample login.conf for COUCHDB ONLY

    hybridRealm {
        net.sf.jkniv.jaas.tomcat.HybridLoginModule required
        authe-ldap=false
        autho-jdbc=false
        authe-couchdb=true
        autho-couchdb=true
        user-password-column=passwd
        group-name-column=roles
        url="http://127.0.0.1:5984/myusers"
        user=admin
        password="5ecret";
    };


        
[1]: https://tomcat.apache.org/tomcat-7.0-doc/realm-howto.html                                              "Configuring Tomcat JAAS"
[2]: https://tomcat.apache.org/tomcat-7.0-doc/config/host.html#Single%20Sign%20On                           "SSO Tomcat"    
[3]: https://docs.oracle.com/javase/7/docs/technotes/guides/security/jaas/tutorials/LoginConfigFile.html    "JAAS Login Configuration File"
[4]: https://docs.oracle.com/javase/7/docs/api/javax/security/auth/login/Configuration.html                 "Login Configuration"
[5]: http://tomcat.apache.org/tomcat-8.0-doc/config/host.html#Single_Sign_On                                "Enable Single Sign On for Tomcat"