Title: JAAS Hybrid Glassfish

Hybrid JAAS for Glassfish
--------------------

The `jkniv-jaas-glassfish` implements a hybrid realm for authentication and authorization model from [JavaTM Authentication and Authorization Service] (http://docs.oracle.com/javase/1.5.0/docs/guide/security/jaas/JAASRefGuide.html) to Glassfish server.

Maven users will need to add the following dependency to their pom.xml for this component:

    <dependency>
      <groupId>net.sf.jkniv</groupId>
      <artifactId>jkniv-jaas-glassfish</artifactId>
      <version>0.2.0</version>
    </dependency>


The hybrid realm are: LDAP and Database. You can use LDAP to authentication and Database for authorization or any combination for them.

| Realm   | Authentication | Authorization |
|---------|----------------|---------------|
|RDBMS    |    supports    |   supports    |
|LDAP     |    supports    |   supports    |


**Note:** It's mandatory to have at least one configured **authentication**, ldap or jdbc.
 

#### Hybrid Realm Properties

| Property             | Default        | Description|
|----------------------|----------------|---------------|
| jaas-context         |                | `hybridRealm` |
| authe-ldap           | `true`        | Enable authentication mode to LDAP |
| authe-jdbc           | `false`       | Enable authentication mode to JDBC |
| autho-ldap           | `false`       | Enable authorization mode to LDAP |
| autho-jdbc           | `true`        | Enable authorization mode to JDBC |
| assign-groups        |                | Comma-separated list of group names. These groups are assigned when the authentication is successfully. |


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
| cipher-algorithm     | `SHA256`      | algorithm for enconde password at database |
| charset              | `UTF-8`       | charset encode for password |
| sql-group            |                | alternative SQL to retrieve the groups from user. Sample: `SELECT GROUP_ID FROM AUTH_GROUP WHERE USERNAME = ? `. The group name must be the first column. |
| sql-password         |                | alternative SQL to retrieve the password from user. Sample: `SELECT PASSWD FROM AUTH_USER WHERE USERNAME = ? `. The password must be the first column.|
| sql-succeeded        |                | An update or insert sql for execute when authenticate login succeeded. |
| sql-failed           |                | An update or insert sql for execute when authenticate login failure. |

**Note:** The version `jkniv-jaas-glassfish-0.1.0` use `@` as placeholder for `=` sql, this is no more necessary.

Sample JDBC tables to authenticate and authorizate users:

    CREATE TABLE "AUTH_USER" 
    ( 
      "USERNAME" VARCHAR2(60) NOT NULL ENABLE, 
      "PASSWD" VARCHAR2(128) NOT NULL ENABLE, 
      CONSTRAINT "PK_USER" PRIMARY KEY ("USERNAME")
    )


    CREATE TABLE "AUTH_GROUP" 
    (    
      "USERNAME" VARCHAR2(60) NOT NULL ENABLE, 
      "GROUP_ID" VARCHAR2(30) NOT NULL ENABLE, 
      CONSTRAINT "PK_GROUP" PRIMARY KEY ("USERNAME", "GROUP_ID")
    )
    
    
#### Configure Custom Realm for Glassfish  

- Copy the jar file `jkniv-jaas-glassfish.jar` to domain lib `glass-install/glassfish4/glassfish/domains/domain1/lib` from glassfish.

- Edit the file `glass-install/glassfish4/glassfish/domains/domain1/config/login.conf` to config the `hybridRealm`. The name `hybridRealm` must be the same value for `jaas-context` at Hybrid Realm Properties.


    hybridRealm {
      net.sf.jkniv.jaas.gf.HybridLoginModule required;
    };
    
- Restart glassfish.

- Enter into glassfish console to config the custom realm and add new realm.

![Glassfish realm properties](realm-config.png)


- Add the properties conform your database and ldap properties. The realm name must be the same used at `<login-config>` from web.xml, and class name must be `net.sf.jkniv.jaas.gf.HybridRealm`.

![Glassfish realm properties](props-config.png)


- Sample entry at `glass-install/glassfish4/glassfish/domains/domain1/config/domain.xml`: 

    <security-service>
        <auth-realm classname="net.sf.jkniv.jaas.gf.HybridRealm" name="acme-realm">
          <property name="group-member-attr" value="memberOf"></property>
          <property name="assign-groups" value="auth"></property>
          <property name="sql-group" value="select role from ROLES where login = ? order by role"></property>
          <property name="directories" value="acme.com.br,acme.com,another.com"></property>
          <property name="datasource-jndi" value="jdbc/myDS"></property>
          <property name="jaas-context" value="hybridRealm"></property>
          <property name="default-domain" value="acme.com"></property>
        </auth-realm>
    </security-service>
    
    
- Sample web.xml from web application


    <login-config>
      <auth-method>FORM</auth-method>
      <realm-name>acme-realm</realm-name>
      <form-login-config>
        <form-login-page>/login.html</form-login-page>
        <form-error-page>/error.html</form-error-page>
      </form-login-config>
    </login-config>
      

    
    
    