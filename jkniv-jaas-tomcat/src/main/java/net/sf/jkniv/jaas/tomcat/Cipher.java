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

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/**
 * Cipher contract to be used for differents algorithms like MD5, SHA-256...
 * 
 * @author Alisson Gomes
 */
public interface Cipher
{
    public static final String MD5 = "MD5";
    public static final String SHA256 = "SHA-256";
    public static final String PLAIN_TEXT = "PLAIN_TEXT";
    public static final String HMACSHA1 = "PBKDF2WithHmacSHA1";
    /**
     * Encrypt the parameter phrase.
     * @param phrase Text to encrypt
     * @return Return a string encrypted
     * @throws UnsupportedEncodingException when charset encode isn't supported
     */
    String encode(String phrase) throws UnsupportedEncodingException;
    
    /**
     * Encrypt the parameter phrase.
     * @param  phrase Text to encrypt
     * @return Return the phrase encrypted at position 0 and salt at position 1.
     * @throws UnsupportedEncodingException when charset encode isn't supported
     */
    String[] encodeWithSalt(String phrase) throws UnsupportedEncodingException;
    
    /**
     * Uncrypt the parameter phrase.
     * @param phrase Text encrypted to uncrypt
     * @return Return a string clean, without cryptographer
     * @throws UnsupportedOperationException when the algorithm doesn't supports decode the phrase.
     */
    String decode(String phrase);
    
    /**
     * Verify if two passwords are equals.
     * @param credentials pair of credentials to check. First parameter it's plain password to be
     * compared with second encoded. If cipher algorithm use a salt token the 3o parameter
     * it's the salt. 
     * @return {@code true} if are equals, {@code false} other wise.
     */
    boolean checkCredential(String... credentials);
    
    /**
     * Check if algorithm can uncypher the value.
     * @return {@code true} when the crypto can uncypher the cypher value, {@code false} otherwise.
     */
    boolean supportDecode();
    
    /**
     * Charset to be used when encode or decode a phrase
     * @return charset used
     */
    Charset getCharset();
    
    /**
     * Algorithm used to encode or decode a phrase.
     * @return name of algorithm
     */
    String getAlgorithm();
}
