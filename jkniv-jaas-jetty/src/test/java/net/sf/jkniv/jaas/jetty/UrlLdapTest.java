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

package net.sf.jkniv.jaas.jetty;

import static org.hamcrest.MatcherAssert.assertThat;

import org.hamcrest.Matchers;

import static org.hamcrest.CoreMatchers.*;


import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

public class UrlLdapTest
{

    @Test
    public void whenParseOneDirectory() throws MalformedURLException
    {
        String url = "acme.com.br";
        String[] directories = splitUrl(url);
        assertThat(directories, notNullValue());
        assertThat(directories.length, is(1));
        assertThat(directories[0], is(url));
        
        String dc = domainComponent(url);
        assertThat(dc, is("dc=acme,dc=com,dc=br"));
    }
    
    @Test
    public void whenParseOneDirectoryWithPort() throws MalformedURLException
    {
        String url = "acme.com.br:389";
        String[] directories = splitUrl(url);
        assertThat(directories, notNullValue());
        assertThat(directories.length, is(1));
        assertThat(directories[0], is(url));
    }

    @Test
    public void whenParseTwoDirectories() throws MalformedURLException
    {
        String urls = "acme.com.br,company.com.br";
        String[] directories = splitUrl(urls);
        assertThat(directories, notNullValue());
        assertThat(directories.length, is(2));
        assertThat(directories[0], is("acme.com.br"));
        assertThat(directories[1], is("company.com.br"));
    }

    @Test
    public void whenParseTwoDirectoriesWithTrim() throws MalformedURLException
    {
        String urls = "acme.com.br , company.com.br ";
        String[] directories = splitUrl(urls);
        assertThat(directories, notNullValue());
        assertThat(directories.length, is(2));
        assertThat(directories[0], is("acme.com.br"));
        assertThat(directories[1], is("company.com.br"));
    }

    @Test(expected=MalformedURLException.class)
    public void whenParsePortError() throws MalformedURLException
    {
        String url = "acme.com.br:a89";
        splitUrl(url);
        Assert.fail();
    }
    
    @Test
    public void whenRegexHasInterrogationSymbol()
    {
        String sql = "select group from table where name ?";
        sql = sql.replaceAll("\\?", "\\= \\?");
        assertThat(sql, is("select group from table where name = ?"));
    }

    @Test
    public void whenRegexHasAtSymbol()
    {
        String sql = "select group from table where name @ ?";
        sql = sql.replaceAll("@", "\\=");
        assertThat(sql, is("select group from table where name = ?"));
    }

    
    private String[] splitUrl(String urls) throws MalformedURLException
    {
        if (urls == null)
            return new String[0];
        
        String[] directories = urls.split(",");
        
        for(int i=0; i<directories.length; i++)
        {
            new URL("http://"+directories[i]);
            directories[i] = directories[i].trim(); 
        }
        
        return directories;
    }
    
    
    private String domainComponent(String url)
    {
        String dc = "";
        int index = url.length();
        int colon = url.indexOf(":");
        if (colon > 0)
            index = colon;
        
        dc = "dc=" + url.substring(0, index).replaceAll("\\.", ",dc=");
        return dc;
    }
}
