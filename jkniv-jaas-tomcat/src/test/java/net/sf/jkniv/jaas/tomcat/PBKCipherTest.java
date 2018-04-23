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
import net.sf.jkniv.jaas.tomcat.Hashing;
import net.sf.jkniv.jaas.tomcat.MD5;
import net.sf.jkniv.jaas.tomcat.PBKDF2WithHmacSHA1;
import net.sf.jkniv.jaas.tomcat.SHA256;
import net.sf.jkniv.jaas.tomcat.Hashing.HashingResult;

public class PBKCipherTest
{    
    @Test
    public void whenEncodeHashingSHA1Works() throws UnsupportedEncodingException
    {
        HashingResult hash = Hashing.createHash("123456");
        
        boolean auth1 = Hashing.validatePassword("0123456", hash.getHash(), hash.getSalt());
        boolean auth2 = Hashing.validatePassword("123456", hash.getHash(), hash.getSalt());
        assertThat(auth1, is(false));
        assertThat(auth2, is(true));
        System.out.println("hash password: " +hash.getHash());
    }
    
    @Test
    public void whenEncodeHashingWithPBK() throws UnsupportedEncodingException
    {
        //HashingResult hash = Hashing.createHash("123456");
        //boolean auth1 = Hashing.validatePassword("0123456", hash.getHash(), hash.getSalt());
        //boolean auth2 = Hashing.validatePassword("123456", hash.getHash(), hash.getSalt());
        Cipher cipher = new PBKDF2WithHmacSHA1();
        
        String[] encoded = cipher.encodeWithSalt("123456");
        boolean checkedTrue = Hashing.validatePassword("123456", encoded[0], encoded[1]);
        
        assertThat(checkedTrue, is(true));
        System.out.println("hash password: " +encoded[0]);
        System.out.println("hash password: " +encoded[1]);
    }

}
