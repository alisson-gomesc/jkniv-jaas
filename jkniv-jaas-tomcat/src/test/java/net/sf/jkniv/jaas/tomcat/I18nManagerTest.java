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
package net.sf.jkniv.jaas.tomcat;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

import net.sf.jkniv.jaas.tomcat.I18nManager;

public class I18nManagerTest
{
    @Test
    public void whenI18nFormatTextSuccessfullyNoParams()
    {
        //hybrid.jdbc.badrealm=HybridLoginModule requires HybridRealm.
        String s = I18nManager.getString("hybrid.jdbc.badrealm");
        assertThat(s, is("HybridLoginModule requires HybridRealm."));
    }

    @Test
    public void whenI18nFormatTextSuccessfully1Params()
    {
        //hybrid.realm.invaliduser=User {1} invalid.
        String s = I18nManager.getString("hybrid.realm.invaliduser", "mary");
        assertThat(s, is("User mary invalid."));
    }

    
    @Test
    public void whenI18nFormatTextSuccessfully2Params()
    {
        //hybrid.jdbc.missingprop=Missing required property {1} for {2}.
        String s = I18nManager.getString("hybrid.jdbc.missingprop", "name", "mandatory");
        assertThat(s, is("Missing required property name for mandatory."));
    }
    
    @Test
    public void whenI18nFormatTextSuccessfully2ParamsTyped()
    {
        //hybrid.jdbc.missingprop=Missing required property {1} for {2}.
        String s = I18nManager.getString("hybrid.jdbc.missingprop", "name", Boolean.TRUE);
        assertThat(s, is("Missing required property name for true."));
    }
    
    
    @Test
    public void whenI18nFormatTextSuccessfully3Params()
    {
        //hybrid.ldap.badconfig=Incomplete configuration of ldap realm: url: {0}, baseDN: {1}, sgroup: {2}
        String s = I18nManager.getString("hybrid.ldap.badconfig", "http://jkniv.me", "users", "admin");
        assertThat(s, is("Incomplete configuration of ldap realm: url: http://jkniv.me, baseDN: users, sgroup: admin"));
    }
    

    @Test
    public void whenI18nFormatTextSuccessfully5ParamsTyped()
    {
        //hybrid.realm.infoauth=try to authenticate user {1}, supportsAuthJdbc={2}, supportsAuthLdap={3}, supportsAuthoJdbc={4}, supportsAuthoLdap={5}
        String s = I18nManager.getString("hybrid.realm.infoauth", "mary:*****", Boolean.valueOf(true), Boolean.valueOf(false),
                        Boolean.valueOf(false), Boolean.valueOf(true));
        assertThat(s, is("try to authenticate user mary:*****, supportsAuthJdbc=true, supportsAuthLdap=false, supportsAuthoJdbc=false, supportsAuthoLdap=true"));
    }
    
    @Test
    public void whenI18nFormatTextSlash()
    {
        //hybrid.test.slash=jdbc/whinstone
        String s = I18nManager.getString("hybrid.test.slash");
        assertThat(s, is("jdbc/whinstone"));
    }
    
    
    
    
}
