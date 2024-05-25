/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server;

/**
 *
 * @author DELL
 */

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class DESencryption {
    // Generate a DES SecretKey
    public static SecretKey generateSecretKey() {
        try {
            SecureRandom secureRandom = SecureRandom.getInstanceStrong();
            byte[] keyBytes = new byte[8]; // DES uses an 8-byte key
            secureRandom.nextBytes(keyBytes);
            return new SecretKeySpec(keyBytes, "DES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}

