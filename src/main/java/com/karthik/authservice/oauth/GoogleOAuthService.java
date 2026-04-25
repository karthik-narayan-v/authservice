package com.karthik.authservice.oauth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.karthik.authservice.exception.CustomException;
import org.springframework.stereotype.Service;

@Service
public class GoogleOAuthService {

    private final GoogleIdTokenVerifier verifier;

    public GoogleOAuthService(GoogleIdTokenVerifier verifier) {
        this.verifier = verifier;
    }

    public GoogleIdToken.Payload verifyToken(String idTokenString) {

        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);

            if (idToken != null) {
                return idToken.getPayload();
            }

        } catch (Exception e) {
            throw new CustomException("Invalid Google token", 401);
        }

        throw new CustomException("Google token verification failed", 401);
    }
}