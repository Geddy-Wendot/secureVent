package com.securevent.core;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Arrays;

public class AESCrypto {

    private static SecretKeySpec secretKey;
    private static final int GCM_IV_LENGTH = 12; // 96 bits
    private static final int GCM_TAG_LENGTH = 128; // 128 bits

    // 1. GENERATE KEY: Turns a simple password into a 256-bit AES Key
    public static void setKey(String myKey) {
        try {
            byte[] key = myKey.getBytes(StandardCharsets.UTF_8);
            // Use SHA-256 to hash the key to a fixed length
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            key = sha.digest(key);
            // Use 32 bytes for AES-256, which is more secure.
            key = Arrays.copyOf(key, 32);
            secretKey = new SecretKeySpec(key, "AES");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 2. ENCRYPT: String = Encrypted String
    public static String encrypt(String strToEncrypt, String secret) {
        try {
            setKey(secret);
            // GCM requires a unique Initialization Vector (IV) for each encryption
            byte[] iv = new byte[GCM_IV_LENGTH];
            new java.security.SecureRandom().nextBytes(iv);
            
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec);
            
            byte[] cipherText = cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8));
            
            // Prepend the IV to the ciphertext for use in decryption
            byte[] cipherTextWithIv = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, cipherTextWithIv, 0, iv.length);
            System.arraycopy(cipherText, 0, cipherTextWithIv, iv.length, cipherText.length);
            
            return Base64.getEncoder().encodeToString(cipherTextWithIv);
        } catch (Exception e) {
            System.out.println("Error while encrypting: " + e.toString());
        }
        return null;
    }

    // 3. DECRYPT: Encrypted String = Original String
    public static String decrypt(String strToDecrypt, String secret) {
        try {
            setKey(secret);
            
            byte[] cipherTextWithIv = Base64.getDecoder().decode(strToDecrypt);
            
            // Extract the IV from the beginning of the byte array
            byte[] iv = Arrays.copyOfRange(cipherTextWithIv, 0, GCM_IV_LENGTH);
            byte[] cipherText = Arrays.copyOfRange(cipherTextWithIv, GCM_IV_LENGTH, cipherTextWithIv.length);
            
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec);
            
            return new String(cipher.doFinal(cipherText));
        } catch (Exception e) {
            // If password is wrong or data is corrupt, this will throw an exception
            return null;
        }
    }
}