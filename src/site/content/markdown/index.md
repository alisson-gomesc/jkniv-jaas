Title: JAAS Hybrid Realm

JAAS Hybrid Realm
--------------------

[Java Authentication and Authorization Service (JAAS)][1] provide mechanism to authentication and authorization the users making your application independent from authentication technologies.

Hybrid Realm provides an interchangeable and combined for authentication and authorization your application, where could be combined Database Realm x LDAP Real x CouchDB Realm:


| Realm   | Authentication | Authorization |
|---------|----------------|---------------|
|LDAP     |    supports    |   supports    | 
|RDBMS    |    supports    |   supports    |
|CouchDB  |    supports    |   supports    | 

### Requirements

`jkniv-jaas-glassfish` and `jkniv-jaas-tomcat` require JDK 1.6 or high.

`jkniv-jaas-jetty` require JDK 1.7 or high.


Maven users will need to add one from following dependency:

for Glassfish server:

    <dependency>
      <groupId>net.sf.jkniv</groupId>
      <artifactId>jkniv-jaas-glassfish</artifactId>
      <version>0.3.6</version>
    </dependency>

for Jetty server:

    <dependency>
      <groupId>net.sf.jkniv</groupId>
      <artifactId>jkniv-jetty-glassfish</artifactId>
      <version>0.3.6</version>
    </dependency>


for Tomcat server:

    <dependency>
      <groupId>net.sf.jkniv</groupId>
      <artifactId>jkniv-jaas-glassfish</artifactId>
      <version>0.3.6</version>
    </dependency>


If your application needs use the Cypher algorithm to encrypt the passwords you can make a reference to `jkniv-jaas-common`:

    <dependency>
      <groupId>net.sf.jkniv</groupId>
      <artifactId>jkniv-jaas-common</artifactId>
      <version>0.3.6</version>
    </dependency>

## Changes in version 0.3.6

### Bug
- ![BUG Fix](images/bug_icon.png "BUG Fix") Authenticate user in LDAP doesn't take the **directories** configuration.
- ![BUG Fix](images/bug_icon.png "BUG Fix")  Authentication using default domain didn't fetch LDAP groups
    
### Improvement
- ![Update](images/update_icon.png "Update") The user's authentication a specific domain could be mandatory authenticate in LDAP
- ![Update](images/update_icon.png "Update") The domain name from email could be mapped to another host, like: algo@`acme.com`  to `ldap.acme.com:386`


**Note:** It's mandatory to have at least one **authentication** mode enable: `authe-ldap`, `authe-jdbc` and/or `authe-couchdb`.
 

#### Hybrid Realm Properties

| Property             | Default        | Description|
|----------------------|----------------|---------------|
| jaas-context         |                | `hybridRealm` |
| authe-ldap           | `true`        | Enable authentication mode for LDAP |
| authe-jdbc           | `false`       | Enable authentication mode for JDBC |
| authe-couchdb        | `false`       | Enable authentication mode for CouchDb |
| autho-ldap           | `false`       | Enable authorization mode for LDAP |
| autho-jdbc           | `true`        | Enable authorization mode for JDBC |
| autho-couchdb        | `false`        | Enable authorization mode for CouchDB |
| assign-groups        |                | Comma-separated list of group names. These groups are assigned when the authentication is successfully. |


There is a `jar` module for each server: Glassfish, Jetty and Tomcat.


| Server                            | Version tested   |
|-----------------------------------|------------------|
| `jkniv-jaas-tomcat` Tomcat      | 7, 8 and 9       |
| `jkniv-jaas-jetty` Jetty        | 9                | 
| `jkniv-jaas-glassfish` Glassfish| 4.1              |    

**Note:** The Tomcat module it’s independently from application server, if you application server no require a inherits from a inner class this module must works for you.


[1]: https://docs.oracle.com/javase/8/docs/technotes/guides/security/jaas/JAASRefGuide.html "Java Authentication and Authorization Service (JAAS)"
[2]: https://tomcat.apache.org/tomcat-9.0-doc/realm-howto.html                              "Realm Configuration HOW-TO"
