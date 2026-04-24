package com.karthik.authservice.service.impl;

import com.karthik.authservice.entity.RefreshToken;
import com.karthik.authservice.entity.User;
import com.karthik.authservice.exception.CustomException;
import com.karthik.authservice.repository.RefreshTokenRepository;
import com.karthik.authservice.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public String createRefreshToken(User user) {
        String token = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .build();

        refreshTokenRepository.save(refreshToken);

        return token;
    }

    @Override
    public String validateRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new CustomException("Invalid refresh token", 400));

        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new CustomException("Refresh token expired", 401);
        }

        return refreshToken.getUser().getId();
    }

    @Override
    public void deleteRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token not found"));

        refreshTokenRepository.delete(refreshToken);
    }
}