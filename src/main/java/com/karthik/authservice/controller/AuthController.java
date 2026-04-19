package com.karthik.authservice.controller;

import com.karthik.authservice.dto.request.LoginRequest;
import com.karthik.authservice.dto.request.SignupRequest;
import com.karthik.authservice.dto.response.AuthResponse;
import com.karthik.authservice.security.jwt.JwtProvider;
import com.karthik.authservice.service.AuthService;
import com.karthik.authservice.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtProvider jwtProvider;
    private final TokenService tokenService;

    @PostMapping("/signup")
    public AuthResponse signup(@RequestBody SignupRequest request) {
        return authService.signup(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public AuthResponse refreshToken(@RequestParam String refreshToken) {

        String userId = tokenService.validateRefreshToken(refreshToken);

        String newAccessToken = jwtProvider.generateToken(userId);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .build();
    }
}