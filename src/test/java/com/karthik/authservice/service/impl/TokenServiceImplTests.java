package com.karthik.authservice.service.impl;

import com.karthik.authservice.entity.RefreshToken;
import com.karthik.authservice.entity.User;
import com.karthik.authservice.exception.CustomException;
import com.karthik.authservice.repository.RefreshTokenRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceImplTests {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private TokenServiceImpl tokenService;

    @Test
    void createRefreshToken_shouldSaveToken_andReturnToken() {

        User user = User.builder().id("user-id").build();

        String token = tokenService.createRefreshToken(user);

        assertNotNull(token);

        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void validateRefreshToken_shouldReturnUserId_whenValid() {

        User user = User.builder().id("user-id").build();

        RefreshToken refreshToken = RefreshToken.builder()
                .token("token")
                .user(user)
                .expiryDate(LocalDateTime.now().plusDays(1))
                .build();

        when(refreshTokenRepository.findByToken("token"))
                .thenReturn(Optional.of(refreshToken));

        String result = tokenService.validateRefreshToken("token");

        assertEquals("user-id", result);
    }

    @Test
    void validateRefreshToken_shouldThrowException_whenTokenNotFound() {

        when(refreshTokenRepository.findByToken("token"))
                .thenReturn(Optional.empty());

        assertThrows(CustomException.class, () ->
                tokenService.validateRefreshToken("token"));
    }

    @Test
    void validateRefreshToken_shouldThrowException_whenExpired() {

        User user = User.builder().id("user-id").build();

        RefreshToken refreshToken = RefreshToken.builder()
                .token("token")
                .user(user)
                .expiryDate(LocalDateTime.now().minusDays(1)) // expired
                .build();

        when(refreshTokenRepository.findByToken("token"))
                .thenReturn(Optional.of(refreshToken));

        assertThrows(CustomException.class, () ->
                tokenService.validateRefreshToken("token"));
    }

    @Test
    void deleteRefreshToken_shouldDeleteToken_whenExists() {

        RefreshToken refreshToken = RefreshToken.builder()
                .token("token")
                .build();

        when(refreshTokenRepository.findByToken("token"))
                .thenReturn(Optional.of(refreshToken));

        tokenService.deleteRefreshToken("token");

        verify(refreshTokenRepository).delete(refreshToken);
    }

    @Test
    void deleteRefreshToken_shouldThrowException_whenNotFound() {

        when(refreshTokenRepository.findByToken("token"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                tokenService.deleteRefreshToken("token"));
    }
}