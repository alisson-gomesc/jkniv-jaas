package net.sf.jkniv.jaas.jetty;


import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.xml.bind.DatatypeConverter;

final class Hashing
{
    
    public static final int ITERATIONS = 1000;
    public static final int SALT_SIZE  = 24;
    public static final int HASH_SIZE  = 24;
    
    public static class HashingResult
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
    
    private Hashing()
    {
    }
    
    private static byte[] function(char[] password, byte[] salt)
    {
        try
        {
            PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, HASH_SIZE * Byte.SIZE);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            System.out.println("Algorithm: "+ factory.getProvider().getName());
            return factory.generateSecret(spec).getEncoded();
        }
        catch (GeneralSecurityException error)
        {
            throw new SecurityException(error);
        }
    }
    
    private static final SecureRandom RANDOM = new SecureRandom();
    
    public static HashingResult createHash(String password)
    {
        byte[] salt = new byte[SALT_SIZE];
        RANDOM.nextBytes(salt);
        byte[] hash = function(password.toCharArray(), salt);
        return new HashingResult(DatatypeConverter.printHexBinary(hash), DatatypeConverter.printHexBinary(salt));
    }
    
    public static boolean validatePassword(String password, String hashHex, String saltHex)
    {
        byte[] hash = DatatypeConverter.parseHexBinary(hashHex);
        byte[] salt = DatatypeConverter.parseHexBinary(saltHex);
        return slowEquals(hash, function(password.toCharArray(), salt));
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
    
}
