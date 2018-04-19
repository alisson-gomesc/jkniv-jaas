/* 
 * JKNIV, whinstone one contract to access your database.
 * 
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
package net.sf.jkniv.jaas.gf.jndi;

import java.util.Properties;

import javax.naming.NamingException;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;

public class JndiCreator
{
    
    /**
     * Properties: jndi, url, driver, username and password to create a BasicDataSource in JNDI.
     * 
     * @param props
     */
    public static void bind()
    {
        try
        {
            BasicDataSource datasource = new BasicDataSource();
            datasource.setUrl("jdbc:oracle:thin:@127.0.0.1:1521:xe");
            datasource.setDriverClassName("oracle.jdbc.driver.OracleDriver");
            datasource.setUsername("whinstone");
            datasource.setPassword("whinstone");
            final SimpleNamingContextBuilder builder = new SimpleNamingContextBuilder();
            builder.bind("jdbc/whinstone",datasource);
            builder.activate();
        }
        catch (NamingException ex)
        {
            ex.printStackTrace();
        }
    }
}