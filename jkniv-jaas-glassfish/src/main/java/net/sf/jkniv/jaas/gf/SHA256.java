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

package net.sf.jkniv.jaas.gf;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

final class SHA256 implements Cipher
{
    private static final String ALGO = Cipher.SHA256;
    private Charset charset;
    private MessageDigest sha256;
    
    public SHA256()
    {
        init();
    }

    public SHA256(Charset charset)
    {
        init();
        this.charset = charset;
    }

    private void init()
    {
        try
        {
            sha256 = MessageDigest.getInstance(ALGO);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException("Cannot get instance of [" + ALGO + "]", e);
        }
        this.charset = Charset.forName("UTF8");
    }
    

    
    public synchronized String encode(String phrase) throws UnsupportedEncodingException
    {
        StringBuffer md5Result = new StringBuffer();
        
        sha256.reset();
        byte[] hash = sha256.digest(phrase.getBytes(charset));
        
        for (int i = 0; i < hash.length; i++) {
            if ((0xff & hash[i]) < 0x10) {
                md5Result.append("0"
                        + Integer.toHexString((0xFF & hash[i])));
            } else {
                md5Result.append(Integer.toHexString(0xFF & hash[i]));
            }
        }
        
        return md5Result.toString();
    }

    public String decode(String phrase)
    {
        throw new UnsupportedOperationException("Cannot decode ["+ALGO+"] algorithm!");
    }
    
    public Charset getCharset()
    {
        return charset;
    }

    public String getAlgorithm()
    {
        return ALGO;
    }
    
    public boolean supportDecode()
    {
        return false;
    }

}
