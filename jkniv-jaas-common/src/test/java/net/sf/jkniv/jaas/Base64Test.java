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

import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Random;


public class Base64Test
{
    private final String PLAIN   = "hello world";
    private final String ENCODED = "aGVsbG8gd29ybGQ=";
    
    @Test
    public void whenBase64Encode() {
        String encoded = new Base64().encode(PLAIN);
        assertThat(encoded, is(ENCODED));
    }
    
    @Test
    public void whenBase64Decode()
    {
        String plain = new Base64().decode(ENCODED);
        assertThat(plain, is(PLAIN));
    }
    
    
    @Test
    public void base64Stuff() {
        Random random = new Random();
        byte[] randomBytes = new byte[32];
        random.nextBytes(randomBytes);

        String internalVersion = com.sun.org.apache.xerces.internal.impl.dv.util.Base64.encode(randomBytes);
        byte[] apacheBytes =  org.apache.commons.codec.binary.Base64.encodeBase64(randomBytes);
        String fromApacheBytes = new String(apacheBytes);

        System.out.println("Internal length = " + internalVersion.length());
        System.out.println("Apache bytes len= " + fromApacheBytes.length());
        System.out.println("Internal version = |" + internalVersion + "|");
        System.out.println("Apache bytes     = |" + fromApacheBytes + "|");
        System.out.println("internal equal apache bytes?: " + internalVersion.equals(fromApacheBytes));
    }
    

}
