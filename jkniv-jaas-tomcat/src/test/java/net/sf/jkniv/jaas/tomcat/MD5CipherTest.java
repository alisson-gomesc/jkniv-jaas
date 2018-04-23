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

import java.io.UnsupportedEncodingException;

import org.junit.Ignore;
import org.junit.Test;

import net.sf.jkniv.jaas.tomcat.Cipher;
import net.sf.jkniv.jaas.tomcat.MD5;
import net.sf.jkniv.jaas.tomcat.SHA256;
import net.sf.jkniv.jaas.tomcat.Hashing.HashingResult;

public class MD5CipherTest
{
    @Test
    public void whenEncodeMD5Works() throws UnsupportedEncodingException
    {
        Cipher md5 = new MD5();
        assertThat(md5.encode("admin"), is("21232f297a57a5a743894a0e4a801fc3"));
    }
        
    @Test(expected = UnsupportedOperationException.class)
    public void whenDeEncodeMD5Works() throws UnsupportedEncodingException
    {
        Cipher md5 = new MD5();
        md5.decode("21232f297a57a5a743894a0e4a801fc3");
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void whenMD5EncodeWithSaltIsUnsupported() throws UnsupportedEncodingException
    {
        Cipher sha256 = new MD5();
        sha256.encodeWithSalt("123456");
    }
}
