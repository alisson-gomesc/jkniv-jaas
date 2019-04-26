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
class LdapConnMockPassword implements LdapConnection
{
    public DirContext openDir(Properties env) throws NamingException
    {
        String validate = env.getProperty("validate", "");
        if ("yes".equalsIgnoreCase(validate))
            return new InitialDirContext(env);
        
        checkUserPassword(env);
        return null;
    }
    
    private void checkUserPassword(Properties env) throws NamingException
    {
        String userWithDomain = env.getProperty(Context.SECURITY_PRINCIPAL, "");
        String password = env.getProperty(Context.SECURITY_CREDENTIALS, "");
        if (!"algo@jkniv.be".equals(userWithDomain))
            throw new NamingException("User [" + userWithDomain + "] doesn't belong this domain");
        if (!"secret".equals(password))
            throw new NamingException("Password [" + password + "] from user [" + userWithDomain + "] doesn't match!");
    }
}
