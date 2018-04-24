/* 
 * JKNIV ,
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

package net.sf.jkniv.jaas.tomcat;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;
import java.util.Properties;

import javax.security.auth.login.LoginException;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import net.sf.jkniv.jaas.tomcat.HybridRealm;
import net.sf.jkniv.jaas.tomcat.JdbcAdapter;
import net.sf.jkniv.jaas.tomcat.jndi.JndiCreator;

public class JdbcAdapterTest
{
    
    @BeforeClass
    public static void setUp()
    {
        JndiCreator.bind();
    }
    
    @Test
    public void testGroups()// throws InvalidOperationException, NoSuchUserException
    {
        JdbcAdapter adapter = new JdbcAdapter(getProps());
        
        List<String> groups = adapter.getGroupNames("admin@localhost");
        
        for(String g : groups)
            System.out.println(g);
    }
    
    @Test// @Ignore("prefix java:/comp/env/")
    public void whenAuthenticationIsSuccessfully() throws LoginException 
    {
        JdbcAdapter realm = new JdbcAdapter(getProps());
        boolean auth = realm.authenticate("admin@localhost", "1234");
        assertThat(auth, is(false));

        auth = realm.authenticate("admin@localhost", "admin");
        assertThat(auth, is(true));
    }
    
    
    private Properties getProps()
    {
        Properties props = new Properties();
        
        props.put(JdbcAdapter.PROP_DATASOURCE_JNDI, "whinstone");
        //props.put(IASRealm.JAAS_CONTEXT_PARAM, "jdbcRealm");
        props.put(JdbcAdapter.PROP_TABLE_USER_COLUMN_PASSWD, "PASSWD");
        props.put(JdbcAdapter.PROP_TABLE_GROUP, "AUTH_GROUP");
        props.put(JdbcAdapter.PROP_TABLE_USER, "AUTH_USER");
        props.put(JdbcAdapter.PROP_TABLE_GROUP_COLUMN_NAME, "GROUP_ID");
        props.put(JdbcAdapter.PROP_TABLE_GROUP_COLUMN_USERNAME, "USERNAME");
        props.put(JdbcAdapter.PROP_TABLE_USER_COLUMN_NAME, "USERNAME");
        props.put(HybridRealm.PROP_ASSIGN_GROUPS, "authorized");
        props.put(JdbcAdapter.PROP_CIPHER_PASSWD, "SHA-256");
        
        return props;
    }
}
