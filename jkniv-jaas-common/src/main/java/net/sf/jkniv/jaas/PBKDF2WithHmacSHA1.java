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

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.xml.bind.DatatypeConverter;


final class PBKDF2WithHmacSHA1 implements Cipher
{
    static final int ITERATIONS = 1000;
    static final int SALT_SIZE  = 24;
    static final int HASH_SIZE  = 24;

    private static final String ALGO = Cipher.HMACSHA1;
    private static final SecureRandom RANDOM = new SecureRandom();
    private Charset charset;
    private SecretKeyFactory factory; 
    
    public PBKDF2WithHmacSHA1()
    {
        init();
    }

    public PBKDF2WithHmacSHA1(Charset charset)
    {
        init();
        this.charset = charset;
    }
    
    private void init()
    {
        try
        {
            factory = SecretKeyFactory.getInstance(ALGO);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException("Cannot get instance of [" + ALGO + "]", e);
        }
        this.charset = Charset.forName("UTF8");
    }
    
    private byte[] function(char[] password, byte[] salt)
    {
        try
        {
            PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, HASH_SIZE * Byte.SIZE);
            return factory.generateSecret(spec).getEncoded();
        }
        catch (GeneralSecurityException error)
        {
            throw new SecurityException(error);
        }
    }

    
    @Override
    public String encode(String phrase) throws UnsupportedEncodingException
    {
        String[] hashs =  encodeWithSalt(phrase);
        return hashs[0];
    }
    
    @Override
    public String[] encodeWithSalt(String phrase) throws UnsupportedEncodingException
    {
        String[] hashs = new String[2];
        byte[] salt = new byte[SALT_SIZE];
        RANDOM.nextBytes(salt);
        byte[] hash = function(phrase.toCharArray(), salt);
        hashs[0] = DatatypeConverter.printHexBinary(hash);
        hashs[1] = DatatypeConverter.printHexBinary(salt);
        return hashs;
    }
    @Override
    public boolean checkCredential(String... credentials)
    {
        String plainCredential = credentials[0];
        String hashedCredential = credentials[1];
        byte[] saltCredential = DatatypeConverter.parseHexBinary(credentials[2]);
        byte[] hashedUserCredential = function(plainCredential.toCharArray(), saltCredential);
        return slowEquals(DatatypeConverter.parseHexBinary(hashedCredential), hashedUserCredential);
    }
    
    @Override
    public String decode(String phrase)
    {
        throw new UnsupportedOperationException("Cannot decode ["+ALGO+"] algorithm!");
    }
    
    @Override
    public boolean supportDecode()
    {
        return false;
    }
    
    @Override
    public Charset getCharset()
    {
        return charset;
    }
    
    @Override
    public String getAlgorithm()
    {
        return ALGO;
    }
    
    private static boolean slowEquals(byte[] a, byte[] b)
    {
        int diff = a.length ^ b.length;
        for (int i = 0; i < a.length && i < b.length; i++)
        {
            diff |= a[i] ^ b[i];
        }
        return diff == 0;
    }

    static class HashingResult
    {
        private final String hash;
        private final String salt;
        
        public HashingResult(String hash, String salt)
        {
            this.hash = hash;
            this.salt = salt;
        }
        
        public String getHash()
        {
            return hash;
        }
        
        public String getSalt()
        {
            return salt;
        }
    }

    @Override
    public boolean hasSalt()
    {
        return true;
    }

}
