package com.karthik.authservice.service.impl;

import com.karthik.authservice.dto.request.LoginRequest;
import com.karthik.authservice.dto.request.SignupRequest;
import com.karthik.authservice.dto.response.AuthResponse;
import com.karthik.authservice.entity.User;
import com.karthik.authservice.repository.UserRepository;
import com.karthik.authservice.security.jwt.JwtProvider;
import com.karthik.authservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Override
    public AuthResponse signup(SignupRequest request) {

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .provider("LOCAL")
                .role("USER")
                .emailVerified(false)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);
        String accessToken = jwtProvider.generateToken(user.getId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken("dummy-refresh")
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String accessToken = jwtProvider.generateToken(user.getId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken("dummy-refresh")
                .build();
    }
}