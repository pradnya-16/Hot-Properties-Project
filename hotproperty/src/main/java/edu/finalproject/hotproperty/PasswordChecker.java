package edu.finalproject.hotproperty;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordChecker {
    public static void main(String[] args) {
        String raw = "admin123";
        String hash = "$2a$10$/SVvkn9CaUJnCjMe1XLpnOwsBUNG.AZ1XZBXTLpJc6wLYLnpQWMCS";

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        boolean matches = encoder.matches(raw, hash);

        System.out.println("✅ Matches? " + matches);
    }
}
