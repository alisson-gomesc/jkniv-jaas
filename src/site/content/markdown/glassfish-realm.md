Title: JAAS Hybrid Glassfish

Hybrid JAAS for Glassfish
--------------------

The `jkniv-jaas-glassfish` implements a hybrid realm for authentication and authorization model from [JavaTM Authentication and Authorization Service] (http://docs.oracle.com/javase/1.5.0/docs/guide/security/jaas/JAASRefGuide.html) to Glassfish server.

Maven users will need to add the following dependency to their pom.xml for this component:

    <dependency>
      <groupId>net.sf.jkniv</groupId>
      <artifactId>jkniv-jaas-glassfish</artifactId>
      <version>0.3.1</version>
    </dependency>
      
    
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
      

    
    
    