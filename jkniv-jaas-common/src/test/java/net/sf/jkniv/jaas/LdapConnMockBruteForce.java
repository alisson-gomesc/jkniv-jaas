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
class LdapConnMockBruteForce implements LdapConnection
{
    private DirContext dirCtx;
    public LdapConnMockBruteForce(DirContext dirCtx)
    {
        this.dirCtx = dirCtx;
    }
    public DirContext openDir(Properties env) throws NamingException
    {
        checkBruteForce(env);
        return dirCtx;
    }
    
    private void checkBruteForce(Properties env) throws NamingException
    {
        String bruteAuth = env.getProperty("brute-auth","a");
        String password = env.getProperty(Context.SECURITY_CREDENTIALS, "b");
        if (!bruteAuth.equals(password))
            throw new NamingException("Brute force authentication doesn't work");
    }
}
