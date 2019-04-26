


Changes in version 0.4.0
-------------------------------------------------------------------------------------------------

** Improvement
    - Supports to Json Web Tokens (JWT) authenticator


Changes in version 0.3.6
-------------------------------------------------------------------------------------------------

** Bug
    - Authenticate user in LDAP doesn't take the **directories** configuration.

** Improvement
    - The user's authentication a specific domain could be mandatory authenticate in LDAP
    - The domain name from email could be mapped to another host, like: algo@`acme.com`  to `ldap.acme.com:386`
   