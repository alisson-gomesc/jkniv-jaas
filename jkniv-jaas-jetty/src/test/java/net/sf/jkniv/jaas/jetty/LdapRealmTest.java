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
package net.sf.jkniv.jaas.jetty;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.security.auth.login.LoginException;

import org.junit.Ignore;
import org.junit.Test;

//import com.sun.enterprise.security.auth.realm.BadRealmException;
//import com.sun.enterprise.security.auth.realm.InvalidOperationException;
//import com.sun.enterprise.security.auth.realm.NoSuchRealmException;
//import com.sun.enterprise.security.auth.realm.NoSuchUserException;

import net.sf.jkniv.jaas.jetty.LdapAdapter;

public class LdapRealmTest
{
    private static final String USER = "alisson.cerqueira";
    private static final String DOMAIN = "mycompany.com.br";
    private static final String PASS = "secret";
    private static final String TO_AUTH = USER+"@"+DOMAIN;
    
    @Test
    public void whenAuthenticationIsSuccessfully() throws LoginException//, BadRealmException, NoSuchRealmException 
    {
        LdapAdapter ldap = new LdapAdapter(getProps());
        boolean auth = ldap.authenticate(TO_AUTH, PASS, false);
        List<String> groups = ldap.getGroupNames(TO_AUTH);
        assertThat(groups.isEmpty(), is(true));
        assertThat(auth, is(true));
    }


    @Test @Ignore
    public void whenFindAndBindGroupsIsSuccessfully() throws LoginException, BadRealmException//, NoSuchRealmException, InvalidOperationException, NoSuchUserException, NamingException  
    {
        Properties props = getProps();
        props.put(Context.SECURITY_PRINCIPAL, TO_AUTH);
        props.put(Context.SECURITY_CREDENTIALS, PASS);
        LdapAdapter ldap = new LdapAdapter(props);
        ldap.authenticate(TO_AUTH, PASS, true);
        List<String> groups = ldap.getGroupNames(TO_AUTH);
        assertThat(groups.isEmpty(), is(false));
        for(String g : groups)
            System.out.println(g);
    }
    
    private Properties getProps()
    {
        Properties props = new Properties();
        props.put(LdapAdapter.PROP_DIRURL, "mycompany.com.br");
        props.put(LdapAdapter.PROP_ATTR_GROUP_MEMBER, "memberOf");
        return props;
    }
}
