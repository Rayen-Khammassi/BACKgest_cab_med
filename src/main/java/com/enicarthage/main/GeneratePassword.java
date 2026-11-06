package com.enicarthage.main;

import org.mindrot.jbcrypt.BCrypt;

public class GeneratePassword {

    public static void main(String[] args) {

        String password = "TestDoc@2026!";

        String hash = BCrypt.hashpw(
                password,
                BCrypt.gensalt(12)
        );

        System.out.println("Mot de passe : " + password);
        System.out.println("Hash BCrypt : " + hash);
    }
}