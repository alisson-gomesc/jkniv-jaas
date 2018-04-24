package net.sf.jkniv.jaas.tomcat;

import java.security.Principal;

public class UserPrincipal implements Principal
{
    private String name;
    private Object credential;
    
    public UserPrincipal(String name, Object credential)
    {
        super();
        this.name = name;
        this.credential = credential;
                
    }
    
    @Override
    public String getName()
    {
        return this.name;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    public Object getCredential()
    {
        return credential;
    }
    
}
