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

import static org.hamcrest.MatcherAssert.assertThat;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;


import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class LdapEntryParserTest
{
    LdapEntryParser parser = new LdapEntryParser();
    
    @Test
    public void whenParserDomainComponents() throws MalformedURLException
    {
        assertThat(parser.dcFrom("acme.com.br"), is("dc=acme,dc=com,dc=br"));
        assertThat(parser.dcFrom("gmail.com"), is("dc=gmail,dc=com"));
        assertThat(parser.dcFrom("gmail.com:386"), is("dc=gmail,dc=com"));
    }

    @Test
    public void whenStripDomain() 
    {
        assertThat(parser.stripDomain("alisson@gmail.com", null), is("gmail.com"));
        assertThat(parser.stripDomain("alisson@gmail.com", "gmail.com.br"), is("gmail.com"));
        assertThat(parser.stripDomain("alisson", "gmail.com"), is("gmail.com"));
    }

    @Test
    public void whenAppendDomain() 
    {
        assertThat(parser.appendDomain("alisson@gmail.com", null), is("alisson@gmail.com"));
        assertThat(parser.appendDomain("alisson@gmail.com", "gmail.com.br"), is("alisson@gmail.com"));
        assertThat(parser.appendDomain("alisson", "gmail.com"), is("alisson@gmail.com"));
        assertThat(parser.appendDomain("alisson", null), is("alisson"));
    }

    @Test
    public void whenSplitUrlParseOneDirectory() throws MalformedURLException
    {
        String url = "acme.com.br";
        String[] directories = parser.splitUrl(url);
        assertThat(directories, notNullValue());
        assertThat(directories.length, is(1));
        assertThat(directories[0], is(url));
    }
    
    @Test
    public void whenSplitUrlOneDirectoryWithPort() throws MalformedURLException
    {
        String url = "acme.com.br:389";
        String[] directories = parser.splitUrl(url);
        assertThat(directories, notNullValue());
        assertThat(directories.length, is(1));
        assertThat(directories[0], is(url));
    }

    @Test
    public void whenSplitUrlTwoDirectories() throws MalformedURLException
    {
        String urls = "acme.com.br,company.com.br";
        String[] directories = parser.splitUrl(urls);
        assertThat(directories, notNullValue());
        assertThat(directories.length, is(2));
        assertThat(directories[0], is("acme.com.br"));
        assertThat(directories[1], is("company.com.br"));
    }

    @Test
    public void whenSplitUrlTwoDirectoriesWithTrim() throws MalformedURLException
    {
        String urls = "acme.com.br , company.com.br ";
        String[] directories = parser.splitUrl(urls);
        assertThat(directories, notNullValue());
        assertThat(directories.length, is(2));
        assertThat(directories[0], is("acme.com.br"));
        assertThat(directories[1], is("company.com.br"));
    }
    
    @Test
    public void whenSplitUrlWithProtocol() throws MalformedURLException
    {
        String urls = "ldap://mycompany.com:386,ldap://mycompany2.com";
        String[] directories = parser.splitUrl(urls);
        assertThat(directories, notNullValue());
        assertThat(directories.length, is(2));
        assertThat(directories[0], is("ldap://mycompany.com:386"));
        assertThat(directories[1], is("ldap://mycompany2.com"));
    }

    @Test
    public void whenSplitUris() throws MalformedURLException
    {
        String urls = "ldap://mycompany.com:386,ldap://mycompany2.com,acme.com,another.com:386, ldaps://mycompany.com:6360, ldaps://mycompany.com";
        URI[] directories = parser.splitUri(urls);
        assertThat(directories, notNullValue());
        assertThat(directories.length, is(6));
        assertThat(directories[0].toString(), is("ldap://mycompany.com:386"));
        assertThat(directories[0].getPort(), is(386));
        assertThat(directories[1].toString(), is("ldap://mycompany2.com:389"));
        assertThat(directories[1].getPort(), is(389));
        assertThat(directories[2].toString(), is("ldap://acme.com:389"));
        assertThat(directories[2].getPort(), is(389));
        assertThat(directories[3].toString(), is("ldap://another.com:386"));
        assertThat(directories[3].getPort(), is(386));
        assertThat(directories[4].toString(), is("ldaps://mycompany.com:6360"));
        assertThat(directories[4].getPort(), is(6360));
        assertThat(directories[5].toString(), is("ldaps://mycompany.com:636"));
        assertThat(directories[5].getPort(), is(636));
    }
    

    @Test(expected=BadRealmException.class)
    public void whenSplitUrlWithPortNotNumberThrowsUrlException() throws MalformedURLException
    {
        String url = "acme.com.br:a89";
        parser.splitUrl(url);
        Assert.fail();
    }

    
}
