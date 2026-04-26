package com.karthik.authservice.email;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("dev")
public class ConsoleEmailService implements EmailService {

    @Override
    public void sendVerificationEmail(String email, String token) {

        System.out.println("=================================");
        System.out.println("EMAIL VERIFICATION (DEV MODE)");
        System.out.println("Email: " + email);
        System.out.println("Verify URL:");
        System.out.println("http://localhost:8081/auth/verify?token=" + token);
        System.out.println("=================================");
    }
}
