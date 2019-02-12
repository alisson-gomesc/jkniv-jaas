Title: JAAS Hybrid Jetty

Hybrid JAAS for Jetty
--------------------

The `jkniv-jaas-jetty` implements a hybrid realm for authentication and authorization model from [JavaTM Authentication and Authorization Service] (http://docs.oracle.com/javase/1.5.0/docs/guide/security/jaas/JAASRefGuide.html) to Jetty server.

Maven users will need to add the following dependency to their pom.xml for this component:

    <dependency>
      <groupId>net.sf.jkniv</groupId>
      <artifactId>jkniv-jaas-jetty</artifactId>
      <version>0.3.1</version>
    </dependency>

    
#### Configure Custom Realm for Jetty  

- Copy the jar file `jkniv-jaas-jetty.jar` to `jetty-install/lib/ext` from jetty.


- Another libraries are addd to `jetty-install/lib/ext` to supports JDBC data source and drivers: `bonecp-0.8.0.RELEASE.jar`, `bonecp-provider-0.8.0-alpha1.jar`, `bonecp-spring-0.8.0.RELEASE.jar`, `guava-19.0.jar`, `ojdbc6-11.2.0.jar`, `slf4j-api-1.7.25.jar` and `slf4j-simple-1.7.21.jar`.

Create new file `jetty-install/etc/login.conf` to config the `hybridRealm`. The name `hybridRealm` must be the same value for `LoginModuleName` at Hybrid Realm Properties.


    hybridRealm {
        net.sf.jkniv.jaas.jetty.HybridLoginModule required
        authe-ldap=false
        authe-jdbc=true
        datasource-jndi="jdbc/myDataSource"
        user-table=AUTH_USER
        user-name-column =USERNAME
        user-password-column =PASSWD
        group-table =AUTH_GROUP
        group-name-column =GROUP_ID
        group-table-user-name-column=USERNAME
        assign-groups=auth;
    };
    
**Note:** Config file from JAAS treat slash as comments, so to config JNDI name from datasource put the key between double quotes like `"jdbc/myDataSource"`    

- Modify the file `jetty-install/etc/jetty-webapp.xml` append a new element `<Call name="addBean">`:


    <Configure id="Server" class="org.eclipse.jetty.server.Server">
      ...
      <Call name="addBean">
        <Arg>
          <New class="org.eclipse.jetty.jaas.JAASLoginService">
            <Set name="name">acme-realm</Set>
            <Set name="LoginModuleName">hybridRealm</Set>
          </New>
        </Arg>
      </Call>

      <Call name="addBean">
        <Arg>
          <New id="dsWhinstone" class="org.eclipse.jetty.plus.jndi.Resource">
            <Arg>jdbc/whinstone</Arg>
            <Arg>
             <New class="com.jolbox.bonecp.BoneCPDataSource">
              <Set name="driverClass">oracle.jdbc.driver.OracleDriver</Set>
              <Set name="jdbcUrl">jdbc:oracle:thin:@127.0.0.1:1521:XE</Set>
              <Set name="username">whinstone</Set>
              <Set name="password">secret</Set>
              <Set name="minConnectionsPerPartition">5</Set>
              <Set name="maxConnectionsPerPartition">50</Set>
              <Set name="acquireIncrement">5</Set>
              <Set name="idleConnectionTestPeriod">30</Set>
             </New>
            </Arg>
          </New>
        </Arg>
      </Call>      
    </Configure>

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


Enable security jaas and jndi for jetty in `jetty-install/start.ini` file:

    --module=plus
    --module=jaas
    jetty.jaas.login.conf=etc/login.conf
    
- start Jetty. (run, forrest, run)


More information to configure [JNDI in Jetty 8] or [JNDI in Jetty 9]

[JNDI in Jetty 8]: https://wiki.eclipse.org/Jetty/Feature/JNDI "Enable JNDI Jetty 8"
[JNDI in Jetty 9]: http://www.eclipse.org/jetty/documentation/current/jndi.html "Enable JNDI Jetty 9"
[Jetty-Jaas]: http://www.eclipse.org/jetty/documentation/current/jaas-support.html "Configuring Jetty JAAS"
    