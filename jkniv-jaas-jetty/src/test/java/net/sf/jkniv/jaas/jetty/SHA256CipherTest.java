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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.UnsupportedEncodingException;

import org.junit.Ignore;
import org.junit.Test;

import net.sf.jkniv.jaas.jetty.Cipher;
import net.sf.jkniv.jaas.jetty.Hashing.HashingResult;
import net.sf.jkniv.jaas.jetty.MD5;
import net.sf.jkniv.jaas.jetty.SHA256;

public class SHA256CipherTest
{
    @Test
    public void whenEncodeSHA256Works() throws UnsupportedEncodingException
    {
        Cipher sha256 = new SHA256();
        assertThat(sha256.encode("admin"), is("8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918"));
    }
        
    @Test(expected = UnsupportedOperationException.class)
    public void whenDeEncodeSHA256Works() throws UnsupportedEncodingException
    {
        Cipher sha256 = new SHA256();
        sha256.decode("8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918");
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void whenSHA256EncodeWithSaltIsUnsupported() throws UnsupportedEncodingException
    {
        Cipher sha256 = new SHA256();
        sha256.encodeWithSalt("123456");
    }
}
