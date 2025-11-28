/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;

public class EncryptionUtil {
    private static final String ALGORITHM = "AES";
    private static final String KEY_STRING = "MySecretKey12345"; // 16 characters for AES-128
    
    private Key secretKey;
    
    public EncryptionUtil() {
        generateKey();
    }
    
    private void generateKey() {
        try {
            // Use a fixed key so all clients can decrypt each other's messages
            byte[] keyBytes = KEY_STRING.getBytes("UTF-8");
            // AES requires 16, 24, or 32 byte key
            byte[] keyBytes16 = new byte[16];
            System.arraycopy(keyBytes, 0, keyBytes16, 0, Math.min(keyBytes.length, 16));
            
            secretKey = new SecretKeySpec(keyBytes16, ALGORITHM);
        } catch (Exception e) {
            throw new RuntimeException("Key generation failed", e);
        }
    }
    
    public String encrypt(String data) {
        try {
            if (data == null || data.isEmpty()) {
                return data;
            }
            
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes("UTF-8"));
            String encrypted = Base64.getEncoder().encodeToString(encryptedBytes);
            System.out.println("Encrypted: '" + data + "' -> '" + encrypted + "'");
            return encrypted;
        } catch (Exception e) {
            System.err.println("Encryption failed: " + e.getMessage());
            return data; // Return original if encryption fails
        }
    }
    
    public String decrypt(String encryptedData) {
        try {
            if (encryptedData == null || encryptedData.isEmpty()) {
                return encryptedData;
            }
            
            // Remove the lock symbol and any extra spaces
            String cleanData = encryptedData.replace("🔒", "").trim();
            
            System.out.println("Attempting to decrypt: '" + cleanData + "'");
            
            // Check if it's actually Base64 encoded
            if (!isBase64(cleanData)) {
                System.out.println("Not Base64 encoded, returning as-is");
                return cleanData;
            }
            
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decodedBytes = Base64.getDecoder().decode(cleanData);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            String result = new String(decryptedBytes, "UTF-8");
            System.out.println("Successfully decrypted to: '" + result + "'");
            return result;
        } catch (Exception e) {
            System.err.println("Decryption failed for: '" + encryptedData + "'");
            System.err.println("Error: " + e.getMessage());
            return "[Cannot decrypt - not an encrypted message]";
        }
    }
    
    private boolean isBase64(String str) {
        try {
            Base64.getDecoder().decode(str);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
