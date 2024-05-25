package main;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class DESencryption {
    // Set the encryption key
    public static SecretKey generateSecretKey() {
        try {
            SecureRandom secureRandom = SecureRandom.getInstanceStrong();
            byte[] keyBytes = new byte[8]; // DES key is 8 bytes
            secureRandom.nextBytes(keyBytes);
            String base64Key = Base64.getEncoder().encodeToString(keyBytes);
            return convertToSecretKey(base64Key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static SecretKey convertToSecretKey(String base64Key) {
        byte[] decodedKey = Base64.getDecoder().decode(base64Key);
        return new SecretKeySpec(decodedKey, "DES");
    }

    public static SecretKey convertToSecretKeyFromBytes(byte[] keyBytes) {
        return new SecretKeySpec(keyBytes, "DES");
    }

    public static String convertSecretKeyToString(SecretKey secretKey) {
        byte[] encodedKey = secretKey.getEncoded();
        return Base64.getEncoder().encodeToString(encodedKey);
    }

    public static SecretKey convertStringToSecretKey(String secretKeyString) {
        byte[] decodedKey = Base64.getDecoder().decode(secretKeyString);
        return new javax.crypto.spec.SecretKeySpec(decodedKey, "DES");
    }

    // Encryption part
    public static String encrypt(String strToEncrypt, SecretKey secretKey) {
        try {
            // Generate a random IV (Initialization Vector)
            byte[] iv = new byte[8]; // DES IV is 8 bytes
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);

            byte[] encryptedBytes = cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8));

            // Combine IV and encrypted data into a single byte array
            byte[] combinedBytes = new byte[iv.length + encryptedBytes.length];
            System.arraycopy(iv, 0, combinedBytes, 0, iv.length);
            System.arraycopy(encryptedBytes, 0, combinedBytes, iv.length, encryptedBytes.length);

            return Base64.getEncoder().encodeToString(combinedBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Decryption part
public static String decrypt(String strToDecrypt, SecretKey secretKey) {
    try {
        if (strToDecrypt == null) {
            throw new IllegalArgumentException("The string to decrypt cannot be null.");
        }

        System.out.println("Received data for decryption: " + strToDecrypt);

        byte[] combinedBytes = Base64.getDecoder().decode(strToDecrypt);

        System.out.println("Decoded bytes length: " + combinedBytes.length);

        if (combinedBytes.length < 8) {
            throw new IllegalArgumentException("Input data is too short.");
        }

        byte[] iv = new byte[8]; // DES IV is 8 bytes
        System.arraycopy(combinedBytes, 0, iv, 0, iv.length);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        if (secretKey == null || secretKey.getEncoded().length != 8) {
            throw new IllegalArgumentException("Invalid secret key.");
        }

        Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);

        byte[] encryptedBytes = new byte[combinedBytes.length - iv.length];
        System.arraycopy(combinedBytes, iv.length, encryptedBytes, 0, encryptedBytes.length);

        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    } catch (Exception e) {
        e.printStackTrace();
        System.err.println("Error decrypting data: " + e.getMessage());
    }
    return null;
}

}
