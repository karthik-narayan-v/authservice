package com.karthik.authservice.email;

import com.karthik.authservice.exception.CustomException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@Profile("prod")
public class ResendEmailService implements EmailService {

    private final RestTemplate restTemplate;

    @Value("${resend.api-key}")
    private String apiKey;

    @Value("${app.base-url}")
    private String baseUrl;

    public ResendEmailService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public void sendVerificationEmail(String email, String token) {

        String verifyUrl = baseUrl + "/auth/verify?token=" + token;

        String url = "https://api.resend.com/emails";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        Map<String, Object> body = Map.of(
                "from", "onboarding@resend.dev",
                "to", email,
                "subject", "Verify your email",
                "html", "<p>Click below to verify:</p>" +
                        "<a href=\"" + verifyUrl + "\">Verify Email</a>"
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(url, request, String.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException("Failed to send verification email", 500);
        }
    }
}