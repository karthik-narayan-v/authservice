package com.karthik.authservice.security.jwt;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtProvider {

    private final ResourceLoader resourceLoader;

    public JwtProvider(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Value("${jwt.private-key:}")
    private String privateKeyStr;

    @Value("${jwt.public-key:}")
    private String publicKeyStr;

    @Value("${jwt.private-key-path:}")
    private String privateKeyPath;

    @Value("${jwt.public-key-path:}")
    private String publicKeyPath;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private String formatKey(String key) {
        return key.replace("\\n", "\n");
    }

    private String readKeyFromFile(String path) throws Exception {
        Resource resource = resourceLoader.getResource(path);
        return new String(resource.getInputStream().readAllBytes());
    }

    private PrivateKey getPrivateKey() throws Exception {

        String keyContent;

        if (privateKeyStr != null && !privateKeyStr.isBlank()) {
            keyContent = formatKey(privateKeyStr);
        } else {
            keyContent = readKeyFromFile(privateKeyPath);
        }

        String key = keyContent
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decoded = Base64.getDecoder().decode(key);

        return KeyFactory.getInstance("RSA")
                .generatePrivate(new PKCS8EncodedKeySpec(decoded));
    }

    private PublicKey getPublicKey() throws Exception {

        String keyContent;

        if (publicKeyStr != null && !publicKeyStr.isBlank()) {
            keyContent = formatKey(publicKeyStr);
        } else {
            keyContent = readKeyFromFile(publicKeyPath);
        }

        String key = keyContent
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decoded = Base64.getDecoder().decode(key);

        return KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(decoded));
    }

    public String generateToken(String userId) {
        try {
            return Jwts.builder()
                    .subject(userId)
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                    .signWith(getPrivateKey()) // RS256
                    .compact();
        } catch (Exception e) {
            throw new RuntimeException("JWT generation failed", e);
        }
    }

    public String getUserIdFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getPublicKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (Exception e) {
            throw new RuntimeException("Invalid JWT", e);
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getPublicKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}