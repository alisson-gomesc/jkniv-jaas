

Changes in version 0.3.6
-------------------------------------------------------------------------------------------------

** Bug
    - Authenticate user in LDAP doesn't take the **directories** configuration.
    - Authentication using default domain didn't fetch LDAP groups
    
** Improvement
    - The user's authentication a specific domain could be mandatory authenticate in LDAP
    - The domain name from email could be mapped to another host, like: algo@`acme.com`  to `ldap.acme.com:386`
   