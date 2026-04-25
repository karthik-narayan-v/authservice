package com.karthik.authservice.oauth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.karthik.authservice.exception.CustomException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GoogleOAuthServiceTests {

    @Mock
    private GoogleIdTokenVerifier verifier;

    @InjectMocks
    private GoogleOAuthService service;

    @Test
    void verifyToken_shouldReturnPayload_whenValidToken() throws Exception {

        GoogleIdToken idToken = mock(GoogleIdToken.class);
        GoogleIdToken.Payload payload = mock(GoogleIdToken.Payload.class);

        when(verifier.verify("valid-token")).thenReturn(idToken);
        when(idToken.getPayload()).thenReturn(payload);

        GoogleIdToken.Payload result = service.verifyToken("valid-token");

        assertNotNull(result);
    }

    @Test
    void verifyToken_shouldThrowException_whenTokenIsNull() throws Exception {

        when(verifier.verify("invalid-token")).thenReturn(null);

        assertThrows(CustomException.class, () ->
                service.verifyToken("invalid-token"));
    }

    @Test
    void verifyToken_shouldThrowException_whenVerifierFails() throws Exception {

        when(verifier.verify("bad-token"))
                .thenThrow(new RuntimeException());

        assertThrows(CustomException.class, () ->
                service.verifyToken("bad-token"));
    }
}