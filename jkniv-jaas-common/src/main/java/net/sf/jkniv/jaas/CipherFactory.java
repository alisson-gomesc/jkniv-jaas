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

import java.nio.charset.Charset;

/**
 * Factory to create new instance of Ciphers algorithms.
 * 
 * @author Alisson Gomes
 * @since 0.3.0
 */
public class CipherFactory
{
    /**
     * new instance of MessageDigest for MD5 algorithm.
     * @return algorithm for MessageDigest MD5
     */
    public static Cipher newMD5()
    {
        return new MD5();
    }
    
    /**
     * new instance of MessageDigest for MD5 algorithm.
     * @param charset charset encode for cryptography
     * @return algorithm for MessageDigest MD5
     */
    public static Cipher newMD5(Charset charset)
    {
        return new MD5(charset);
    }

    /**
     * new instance of MessageDigest for SHA-256 algorithm.
     * @return algorithm for MessageDigest SHA-256
     */
    public static Cipher newSHA256()
    {
        return new SHA256();
    }
    

    /**
     * new instance of MessageDigest for SHA-256 algorithm.
     * @param charset charset encode for cryptography
     * @return algorithm for MessageDigest SHA-256
     */
    public static Cipher newSHA256(Charset charset)
    {
        return new SHA256(charset);
    }

    /**
     * new instance of password-based encryption (PBE) algorithm.
     * @return algorithm for PBE HMAC-SHA1
     */
    public static Cipher newHmacSHA1()
    {
        return new PBKDF2WithHmacSHA1();
    }
    
    /**
     * new instance of plain text (no encrypt).
     * @return algorithm for plain text
     */
    public static Cipher newPlainText()
    {
        return new PlainText();
    }


    /**
     * new instance of plain text (no encrypt).
     * @param charset charset encode for cryptography
     * @return algorithm for plain text
     */
    public static Cipher newPlainText(Charset charset)
    {
        return new PlainText(charset);
    }
}
