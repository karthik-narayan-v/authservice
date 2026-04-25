package com.karthik.authservice.config;

import com.karthik.authservice.controller.AuthController;
import com.karthik.authservice.controller.UserController;
import com.karthik.authservice.dto.response.AuthResponse;
import com.karthik.authservice.repository.UserRepository;
import com.karthik.authservice.security.jwt.JwtProvider;
import com.karthik.authservice.security.user.CustomUserDetails;
import com.karthik.authservice.service.AuthService;
import com.karthik.authservice.service.TokenService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SecurityConfigIntegrationTests {

    private MockMvc mockMvc;

    private AuthService authService;

    @BeforeEach
    void setup() {

        // 🔥 Mock dependencies
        authService = mock(AuthService.class);
        TokenService tokenService = mock(TokenService.class);
        JwtProvider jwtProvider = mock(JwtProvider.class);
        UserRepository userRepository = mock(UserRepository.class);

        // 🔥 Create controller with mocks
        AuthController authController =
                new AuthController(authService, jwtProvider, tokenService, userRepository);

        UserController userController = new UserController();

        mockMvc = MockMvcBuilders
                .standaloneSetup(authController, userController)
                .build();
    }

    // ✅ AUTH endpoint test
    @Test
    void authEndpoints_shouldBeAccessible() throws Exception {

        String requestBody = """
            {
              "email": "test@gmail.com",
              "password": "123456"
            }
        """;

        when(authService.login(any()))
                .thenReturn(AuthResponse.builder()
                        .accessToken("token")
                        .refreshToken("refresh")
                        .build());

        mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("token"));
    }

    // ✅ PROTECTED endpoint test
    @Test
    void protectedEndpoints_shouldExist() throws Exception {

        CustomUserDetails userDetails = new CustomUserDetails(
                com.karthik.authservice.entity.User.builder()
                        .email("test@gmail.com")
                        .build()
        );

        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );

        mockMvc.perform(get("/users/me")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(content().string("User Email: test@gmail.com"));
    }
}