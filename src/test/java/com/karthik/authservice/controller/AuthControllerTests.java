package com.karthik.authservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.karthik.authservice.dto.request.LoginRequest;
import com.karthik.authservice.dto.request.SignupRequest;
import com.karthik.authservice.dto.response.AuthResponse;
import com.karthik.authservice.security.jwt.JwtProvider;
import com.karthik.authservice.service.AuthService;
import com.karthik.authservice.service.TokenService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTests {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AuthService authService;

    @Mock
    private TokenService tokenService;

    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    // 🔐 SIGNUP
    @Test
    void signup_shouldReturnAuthResponse() throws Exception {

        SignupRequest request = new SignupRequest("test@gmail.com", "123456");

        AuthResponse response = AuthResponse.builder()
                .accessToken("access")
                .refreshToken("refresh")
                .build();

        when(authService.signup(any())).thenReturn(response);

        mockMvc.perform(post("/auth/signup")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access"));
    }

    // 🔐 LOGIN
    @Test
    void login_shouldReturnTokens() throws Exception {

        LoginRequest request = new LoginRequest("test@gmail.com", "123456");

        AuthResponse response = AuthResponse.builder()
                .accessToken("access")
                .refreshToken("refresh")
                .build();

        when(authService.login(any())).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access"));
    }

    // 🔁 REFRESH TOKEN
    @Test
    void refreshToken_shouldReturnNewAccessToken() throws Exception {

        when(tokenService.validateRefreshToken("refresh"))
                .thenReturn("user-id");

        when(jwtProvider.generateToken("user-id"))
                .thenReturn("new-access");

        mockMvc.perform(post("/auth/refresh")
                        .param("refreshToken", "refresh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access"));
    }

    // 🚪 LOGOUT
    @Test
    void logout_shouldReturnSuccessMessage() throws Exception {

        mockMvc.perform(post("/auth/logout")
                        .param("refreshToken", "refresh"))
                .andExpect(status().isOk())
                .andExpect(content().string("Logged out successfully"));

        verify(tokenService).deleteRefreshToken("refresh");
    }

    // 🔐 FORGOT PASSWORD
    @Test
    void forgotPassword_shouldCallService() throws Exception {

        mockMvc.perform(post("/auth/forgot-password")
                        .param("email", "test@gmail.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("Password reset link sent"));

        verify(authService).forgotPassword("test@gmail.com");
    }

    // 🔐 RESET PASSWORD
    @Test
    void resetPassword_shouldCallService() throws Exception {

        mockMvc.perform(post("/auth/reset-password")
                        .param("token", "abc")
                        .param("newPassword", "123456"))
                .andExpect(status().isOk())
                .andExpect(content().string("Password reset successful"));

        verify(authService).resetPassword("abc", "123456");
    }

    // 🌐 GOOGLE LOGIN
    @Test
    void googleLogin_shouldReturnTokens() throws Exception {

        AuthResponse response = AuthResponse.builder()
                .accessToken("access")
                .refreshToken("refresh")
                .build();

        when(authService.googleLogin("token")).thenReturn(response);

        mockMvc.perform(post("/auth/google")
                        .param("idToken", "token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access"));
    }
}