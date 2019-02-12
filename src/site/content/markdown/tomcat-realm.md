Title: JAAS Hybrid Tomcat

Hybrid JAAS for Tomcat
--------------------

The `jkniv-jaas-tomcat` implements a hybrid realm for authentication and authorization model from [JavaTM Authentication and Authorization Service] (http://docs.oracle.com/javase/1.5.0/docs/guide/security/jaas/JAASRefGuide.html) to Tomcat server.

Maven users will need to add the following dependency to their pom.xml for this component:

    <dependency>
      <groupId>net.sf.jkniv</groupId>
      <artifactId>jkniv-jaas-tomcat</artifactId>
      <version>0.3.1</version>
    </dependency>

     
#### Configure Custom Realm for Tomcat  

Copy the jar files `jkniv-jaas-tomcat.jar` to common lib `tomcat-install/lib` from Tomcat:
 
Create new file `tomcat-install/conf/login.conf` to config the `hybridRealm`. The name `hybridRealm` must be the same value from *appName* attribute `<Realm appName="hybridRealm"` in `server.xml`.


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
        group-table-user-name-column=USERNAME
        assign-groups=auth;
    };

**Note:** Config file from JAAS treat slash as comments, so to config JNDI name from datasource put the key between double quotes like `"java:/comp/env/jdbc/myDataSource"`

- Modify the file `tomcat-install/conf/server.xml` append a new ream `<Realm` element:


    <Engine defaultHost="localhost" name="Catalina">
      ...
      <Realm className="org.apache.catalina.realm.JAASRealm"
        appName="hybridRealm"
        userClassNames="net.sf.jkniv.jaas.tomcat.UserPrincipal"
        roleClassNames="net.sf.jkniv.jaas.tomcat.RolePrincipal">
      </Realm>
    </Engine>

- Modify `web.xml` from your application configuring the new realm `acme-realm`:
    
    <security-role>
      <description>Any user authenticated</description>
      <role-name>auth</role-name>
    </security-role>  
    <login-config> 
      <auth-method>FORM</auth-method> 
      <realm-name>acme-realm</realm-name> 
      <form-login-config> 
        <form-login-page>/login.html</form-login-page> 
        <form-error-page>/error.html</form-error-page> 
      </form-login-config> 
    </login-config> 
    <security-constraint>
      <web-resource-collection>
        <web-resource-name>Exclude from Security</web-resource-name>
        <url-pattern>/api/*</url-pattern>
      </web-resource-collection>
      <auth-constraint>
        <role-name>auth</role-name>
      </auth-constraint>
    </security-constraint>

- Set up Tomcat to to find `tomcat-install/conf/login.conf` file specifying its location to the JVM, for instance by setting the environment variable: JAVA_OPTS=$JAVA_OPTS -Djava.security.auth.login.config==$CATALINA_BASE/conf/login.conf
    
- start Tomcat (run, forrest, run)

*Note:* If [Enable Single Sign On for Tomcat][5] it's a requirement uncomment the `<Valve>`element from `tomcat-install/conf/server.xml` file.


    <Host name="localhost" ...>
     ...
     <Valve className="org.apache.catalina.authenticator.SingleSignOn"/>
     ...
    </Host>
    
    
        
[1]: https://tomcat.apache.org/tomcat-7.0-doc/realm-howto.html                                              "Configuring Tomcat JAAS"
[2]: https://tomcat.apache.org/tomcat-7.0-doc/config/host.html#Single%20Sign%20On                           "SSO Tomcat"    
[3]: https://docs.oracle.com/javase/7/docs/technotes/guides/security/jaas/tutorials/LoginConfigFile.html    "JAAS Login Configuration File"
[4]: https://docs.oracle.com/javase/7/docs/api/javax/security/auth/login/Configuration.html                 "Login Configuration"
[5]: http://tomcat.apache.org/tomcat-8.0-doc/config/host.html#Single_Sign_On                                "Enable Single Sign On for Tomcat"