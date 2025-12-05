package org.example.algo_proje.utils;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class Security {

    // Salt üret
    public static byte[] generateSalt() {
        byte[] salt = new byte[32];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    // Hash üret (SHA-256 + Salt)
    public static byte[] hashPassword(String password, byte[] salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            return md.digest(password.getBytes());
        } catch (Exception e) {
            return null;
        }
    }

    // Hash karşılaştır
    public static boolean verifyPassword(String password, byte[] salt, byte[] expectedHash) {
        byte[] newHash = hashPassword(password, salt);
        if (newHash.length != expectedHash.length)
            return false;

        for (int i = 0; i < expectedHash.length; i++) {
            if (newHash[i] != expectedHash[i]) return false;
        }

        return true;
    }
}