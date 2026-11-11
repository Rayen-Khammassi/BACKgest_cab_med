package com.enicarthage.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordGenerator {

    public static String hash(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(12));
    }

    public static boolean verify(String rawPassword, String hashedPassword) {
        if (rawPassword == null || hashedPassword == null) return false;
        return BCrypt.checkpw(rawPassword, hashedPassword);
    }

    public static void main(String[] args) {
        String password = "Test123!";
        String hashedPassword = hash(password);

        System.out.println("Password: " + password);
        System.out.println("BCrypt hash: " + hashedPassword);
    }
}
