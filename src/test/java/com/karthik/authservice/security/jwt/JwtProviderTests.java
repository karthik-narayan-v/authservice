package com.karthik.authservice.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class JwtProviderTests {

    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() throws Exception {
        jwtProvider = new JwtProvider();

        // Set private fields using reflection
        setField(jwtProvider, "jwtSecret", "my-secret-key-my-secret-key-my-secret-key");
        setField(jwtProvider, "jwtExpiration", 3600000L); // 1 hour
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void generateToken_shouldReturnValidToken() {

        String token = jwtProvider.generateToken("user-id");

        assertNotNull(token);
        assertTrue(token.length() > 20);
    }

    @Test
    void getUserIdFromToken_shouldReturnCorrectUserId() {

        String token = jwtProvider.generateToken("user-id");

        String userId = jwtProvider.getUserIdFromToken(token);

        assertEquals("user-id", userId);
    }

    @Test
    void validateToken_shouldReturnTrue_forValidToken() {

        String token = jwtProvider.generateToken("user-id");

        boolean isValid = jwtProvider.validateToken(token);

        assertTrue(isValid);
    }

    @Test
    void validateToken_shouldReturnFalse_forInvalidToken() {

        boolean isValid = jwtProvider.validateToken("invalid.token.here");

        assertFalse(isValid);
    }

    @Test
    void validateToken_shouldReturnFalse_forExpiredToken() throws Exception {

        JwtProvider provider = new JwtProvider();

        setField(provider, "jwtSecret", "my-secret-key-my-secret-key-my-secret-key");
        setField(provider, "jwtExpiration", 1L); // 1 ms

        String token = provider.generateToken("user-id");

        Thread.sleep(5); // wait for expiry

        boolean isValid = provider.validateToken(token);

        assertFalse(isValid);
    }
}