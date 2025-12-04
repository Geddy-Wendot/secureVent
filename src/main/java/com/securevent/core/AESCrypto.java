package com.securevent.core;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Arrays;

public class AESCrypto {

    private static SecretKeySpec secretKey;

    // 1. GENERATE KEY: Turns a simple password into a 256-bit AES Key
    public static void setKey(String myKey) {
        try {
            byte[] key = myKey.getBytes(StandardCharsets.UTF_8);
            // Use SHA-1 or SHA-256 to hash the key to a fixed length
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            key = sha.digest(key);
            // AES needs 16 bytes (128 bit) or 32 bytes (256 bit). 
            // We slice the first 16 bytes for AES-128 (Faster/Simpler)
            key = Arrays.copyOf(key, 16); 
            secretKey = new SecretKeySpec(key, "AES");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 2. ENCRYPT: String = Encrypted String
    public static String encrypt(String strToEncrypt, String secret) {
        try {
            setKey(secret);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            // Return Base64 string so it's safe to transport
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            System.out.println("Error while encrypting: " + e.toString());
        }
        return null;
    }

    // 3. DECRYPT: Encrypted String = Original String
    public static String decrypt(String strToDecrypt, String secret) {
        try {
            setKey(secret);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        } catch (Exception e) {
            // If password is wrong, this usually throws a "BadPaddingException"
            return null;
        }
    }
}