

Changes in version 0.3.6
-------------------------------------------------------------------------------------------------

### Bug
- ![BUG Fix](images/bug_icon.png "BUG Fix") Authenticate user in LDAP doesn't take the **directories** configuration.
- ![BUG Fix](images/bug_icon.png "BUG Fix")  Authentication using default domain didn't fetch LDAP groups
    
### Improvement
- ![Update](images/update_icon.png "Update") The user's authentication a specific domain could be mandatory authenticate in LDAP
- ![Update](images/update_icon.png "Update") The domain name from email could be mapped to another host, like: `acme.com`  to `ldap.acme.com:386`
   