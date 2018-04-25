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

package net.sf.jkniv.jaas;

import static org.hamcrest.MatcherAssert.assertThat;

import org.hamcrest.Matchers;

import static org.hamcrest.CoreMatchers.*;


import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

public class StringSqlTest
{

    @Test
    public void whenSplitSqlInParts() throws MalformedURLException
    {
        String sql1 = "select role from users u";
        String sql2 = "select role from users u where u.name = ?";
        String sql3 = "select role from users u where u.name = ? union all select role from where r.user = ?";
        String sql4 = "select role from users u where u.name = ? order by role";
        String sql5 = "select role from users u where u.name = ? union all select role from where r.user = ? order by role";
        String[] parts1 = sql1.split("\\?");
        String[] parts2 = sql2.split("\\?");
        String[] parts3 = sql3.split("\\?");
        String[] parts4 = sql4.split("\\?");
        String[] parts5 = sql5.split("\\?");
        
        assertThat(parts1.length, is(1)); assertThat(countParams(sql1), is(0));
        assertThat(parts2.length, is(1)); assertThat(countParams(sql2), is(1));
        assertThat(parts3.length, is(2)); assertThat(countParams(sql3), is(2));
        assertThat(parts4.length, is(2)); assertThat(countParams(sql4), is(1));
        assertThat(parts5.length, is(3)); assertThat(countParams(sql5), is(2));

    }

    private int countParams(String sql)
    {
        int params = 0;
        for (int i=0; i<sql.length();i++){
            if (sql.charAt(i) == '?')
                params++;
        }
        return params;
    }
}
