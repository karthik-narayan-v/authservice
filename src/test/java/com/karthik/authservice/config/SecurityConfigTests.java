package com.karthik.authservice.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class SecurityConfigTests {

    private final SecurityConfig config = new SecurityConfig(null, null);

    @Test
    void passwordEncoder_shouldEncodePassword() {

        PasswordEncoder encoder = config.passwordEncoder();

        String raw = "123456";
        String encoded = encoder.encode(raw);

        assertNotNull(encoded);
        assertNotEquals(raw, encoded);
        assertTrue(encoder.matches(raw, encoded));
    }
}