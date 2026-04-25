package com.karthik.authservice.service.impl;

import com.karthik.authservice.dto.request.LoginRequest;
import com.karthik.authservice.dto.request.SignupRequest;
import com.karthik.authservice.entity.User;
import com.karthik.authservice.exception.CustomException;
import com.karthik.authservice.oauth.GoogleOAuthService;
import com.karthik.authservice.repository.UserRepository;
import com.karthik.authservice.security.jwt.JwtProvider;
import com.karthik.authservice.service.TokenService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private TokenService tokenService;

    @Mock
    private GoogleOAuthService googleOAuthService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void signup_shouldCreateUser_andReturnTokens() {

        SignupRequest request = new SignupRequest("test@gmail.com", "123456");

        when(passwordEncoder.encode("123456")).thenReturn("encodedPass");

        User savedUser = User.builder()
                .id("user-id")
                .email("test@gmail.com")
                .password("encodedPass")
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtProvider.generateToken("user-id")).thenReturn("access-token");
        when(tokenService.createRefreshToken(any(User.class))).thenReturn("refresh-token");

        var response = authService.signup(request);

        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());

        verify(userRepository).save(any(User.class));
    }

    @Test
    void login_shouldReturnTokens_whenValidCredentials() {

        LoginRequest request = new LoginRequest("test@gmail.com", "123456");

        User user = User.builder()
                .id("user-id")
                .email("test@gmail.com")
                .password("encodedPass")
                .emailVerified(true)
                .build();

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("123456", "encodedPass")).thenReturn(true);

        when(jwtProvider.generateToken("user-id")).thenReturn("access-token");
        when(tokenService.createRefreshToken(user)).thenReturn("refresh-token");

        var response = authService.login(request);

        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
    }

    @Test
    void login_shouldThrowException_whenUserNotFound() {

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.empty());

        assertThrows(CustomException.class, () ->
                authService.login(new LoginRequest("test@gmail.com", "123")));
    }

    @Test
    void login_shouldThrowException_whenEmailNotVerified() {

        User user = User.builder()
                .email("test@gmail.com")
                .emailVerified(false)
                .build();

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(user));

        assertThrows(CustomException.class, () ->
                authService.login(new LoginRequest("test@gmail.com", "123")));
    }

    @Test
    void login_shouldThrowException_whenInvalidPassword() {

        User user = User.builder()
                .email("test@gmail.com")
                .password("encoded")
                .emailVerified(true)
                .build();

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertThrows(CustomException.class, () ->
                authService.login(new LoginRequest("test@gmail.com", "wrong")));
    }

    @Test
    void resetPassword_shouldUpdatePassword() {

        User user = User.builder()
                .resetToken("token")
                .resetTokenExpiry(java.time.LocalDateTime.now().plusMinutes(10))
                .build();

        when(userRepository.findByResetToken("token"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.encode("newpass")).thenReturn("encoded");

        authService.resetPassword("token", "newpass");

        assertNull(user.getResetToken());
        verify(userRepository).save(user);
    }

    @Test
    void googleLogin_shouldCreateUser_ifNotExists() {

        var payload = mock(com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload.class);
        when(payload.getEmail()).thenReturn("google@gmail.com");

        when(googleOAuthService.verifyToken("idToken")).thenReturn(payload);
        when(userRepository.findByEmail("google@gmail.com"))
                .thenReturn(Optional.empty());

        User newUser = User.builder()
                .id("user-id")
                .email("google@gmail.com")
                .build();

        when(userRepository.save(any(User.class))).thenReturn(newUser);

        when(jwtProvider.generateToken("user-id")).thenReturn("access");
        when(tokenService.createRefreshToken(any())).thenReturn("refresh");

        var response = authService.googleLogin("idToken");

        assertEquals("access", response.getAccessToken());
    }
}