package org.orbitalLogistic.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String password = "1234"; // админка
        String hash = encoder.encode(password);

        System.out.println("Password: " + password);
        System.out.println("BCrypt hash: " + hash);
        System.out.println("\nAdd this to .env:");
        System.out.println("ADMIN_PASSWORD=" + hash);
    }
}
