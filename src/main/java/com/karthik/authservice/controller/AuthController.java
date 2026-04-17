package com.karthik.authservice.controller;

import com.karthik.authservice.dto.request.LoginRequest;
import com.karthik.authservice.dto.request.SignupRequest;
import com.karthik.authservice.dto.response.AuthResponse;
import com.karthik.authservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public AuthResponse signup(@RequestBody SignupRequest request) {
        return authService.signup(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }
}