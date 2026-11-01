
package com.enicarthage.main;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordGenerator {

    public static void main(String[] args) {

        String password = "Test123!";

        String hashedPassword = BCrypt.hashpw(
                password,
                BCrypt.gensalt(12)
        );

        System.out.println("Password: " + password);
        System.out.println("BCrypt hash: " + hashedPassword);
    }
}
