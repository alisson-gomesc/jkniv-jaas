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

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.security.auth.login.LoginException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import net.sf.jkniv.jaas.BadRealmException;
import net.sf.jkniv.jaas.LdapAdapter;

@SuppressWarnings("rawtypes")
public class LdapRealmTest
{
    private static final String USER = "algo";
    private static final String DOMAIN = "jkniv.be";
    private static final String PASS = "secret";
    private static final String TO_AUTH = USER+"@"+DOMAIN;
    
    private LdapConnection ldapConn;
    private DirContext ctx;
    private NamingEnumeration answer;
    private NamingEnumeration ae;
    //private SearchControls ctls;
    private SearchResult sr;
    private Attributes attrs;
    private Attribute attr;
    private NamingEnumeration eAttr;
    
    @Before
    public void setUp() throws Exception 
    {
        this.ldapConn = mock(LdapConnection.class);
        this.ctx = mock(DirContext.class);
        this.answer = mock(NamingEnumeration.class);
        this.sr = mock(SearchResult.class);
        this.attrs = mock(Attributes.class);
        this.ae = mock(NamingEnumeration.class);
        this.attr = mock(Attribute.class);
        this.eAttr = mock(NamingEnumeration.class);

        given(ldapConn.getDirContext(any(Properties.class))).willReturn(this.ctx);
        given(this.ctx.search(eq("dc=jkniv,dc=be"), eq("mail=algo@jkniv.be"), any(SearchControls.class)))
            .willReturn(this.answer);
        doNothing().when(this.ctx).close();
        given(answer.hasMore()).willReturn(true);
        given(answer.next()).willReturn(sr);
        given(sr.getAttributes()).willReturn(attrs);

        given(attrs.getAll()).willReturn(ae); 
        given(ae.hasMore()).willReturn(true, true, false);
        given(ae.next()).willReturn(attr);
        
        given(attr.getAll()).willReturn(eAttr);
        given(attr.getID()).willReturn("memberOf");
        
        given(eAttr.hasMore()).willReturn(true, true, false);
        given(eAttr.next()).willReturn("CN=Programmer,OU=JKNIV,DC=jkniv,DC=be", 
                                       "CN=Architect,OU=JKNIV,DC=jkniv,DC=be");
    }
    
    @Test
    public void whenAuthenticationIsSuccessfully() throws LoginException//, BadRealmException, NoSuchRealmException 
    {
        LdapAdapter ldap = new LdapAdapter(getProps(), this.ldapConn);
        boolean auth = ldap.authenticate(TO_AUTH, PASS, false);
        List<String> groups = ldap.getGroupNames(TO_AUTH);
        assertThat(groups.isEmpty(), is(true));
        assertThat(auth, is(true));
    }


    @Test
    public void whenFindAndBindGroupsIsSuccessfully() throws LoginException, BadRealmException//, NoSuchRealmException, InvalidOperationException, NoSuchUserException, NamingException  
    {
        Properties props = getProps();
        props.put(Context.SECURITY_PRINCIPAL, TO_AUTH);
        props.put(Context.SECURITY_CREDENTIALS, PASS);
        LdapAdapter ldap = new LdapAdapter(props, this.ldapConn);
        ldap.authenticate(TO_AUTH, PASS, true);
        List<String> groups = ldap.getGroupNames(TO_AUTH);
        assertThat(groups.isEmpty(), is(false));
        assertThat(groups, hasItems("Programmer","Architect"));
    }
    
    private Properties getProps()
    {
        Properties props = new Properties();
        props.put(LdapAdapter.PROP_DIRURL, "jkniv.be");
        props.put(LdapAdapter.PROP_ATTR_GROUP_MEMBER, "memberOf");
        return props;
    }
}
