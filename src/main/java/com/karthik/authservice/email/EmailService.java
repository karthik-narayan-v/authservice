package com.karthik.authservice.email;

public interface EmailService {
    void sendVerificationEmail(String email, String token);
}