/* 
 * JKNIV JAAS,
 * Copyright (C) 2017, the original author or authors.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package net.sf.jkniv.jaas;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

/**
 * Represent a connection with LDAP with search capability.
 * 
 * @author Alisson Gomes
 *
 */
class LdapConnMockPassword implements LdapConnection
{
    private DirContext dirCtx;
    private final Map<String, String> USERS = new HashMap<String, String>();
    
    public LdapConnMockPassword(DirContext dirCtx)
    {
        this.dirCtx = dirCtx;
        USERS.put("algo@jkniv.be", "secret");
        USERS.put("algo@jkniv.io", "ultra-secret");
    }
    public DirContext openDir(Properties env) throws NamingException
    {
        checkUserPassword(env);
        return dirCtx;
    }
    
    private void checkUserPassword(Properties env) throws NamingException
    {
        String userWithDomain = env.getProperty(Context.SECURITY_PRINCIPAL, "");
        String password = env.getProperty(Context.SECURITY_CREDENTIALS, "");
        String passReal = USERS.get(userWithDomain);
        if (passReal == null)
            throw new NamingException("User [" + userWithDomain + "] doesn't belong this domain");
        if (!passReal.equals(password))
            throw new NamingException("Password [" + password + "] from user [" + userWithDomain + "] doesn't match!");
    }
}
