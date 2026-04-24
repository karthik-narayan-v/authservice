package com.karthik.authservice.service.impl;

import com.karthik.authservice.dto.request.LoginRequest;
import com.karthik.authservice.dto.request.SignupRequest;
import com.karthik.authservice.dto.response.AuthResponse;
import com.karthik.authservice.entity.User;
import com.karthik.authservice.exception.CustomException;
import com.karthik.authservice.oauth.GoogleOAuthService;
import com.karthik.authservice.repository.UserRepository;
import com.karthik.authservice.security.jwt.JwtProvider;
import com.karthik.authservice.service.AuthService;
import com.karthik.authservice.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final TokenService tokenService;
    private final GoogleOAuthService googleOAuthService;

    @Override
    public AuthResponse signup(SignupRequest request) {

        String verificationToken = UUID.randomUUID().toString();

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .provider("LOCAL")
                .role("USER")
                .emailVerified(false)
                .verificationToken(verificationToken)
                .createdAt(LocalDateTime.now())
                .build();

        System.out.println("Verify email using: http://localhost:8081/auth/verify?token=" + verificationToken);

        userRepository.save(user);
        String accessToken = jwtProvider.generateToken(user.getId());
        String refreshToken = tokenService.createRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException("User not found", 404));

        if (!user.isEmailVerified()) {
            throw new CustomException("Email not verified", 403);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException("Invalid credentials", 401);
        }

        String accessToken = jwtProvider.generateToken(user.getId());
        String refreshToken = tokenService.createRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public void forgotPassword(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = UUID.randomUUID().toString();

        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(15));

        userRepository.save(user);

        System.out.println("Reset password link: http://localhost:8081/auth/reset-password?token=" + token);
    }

    @Override
    public void resetPassword(String token, String newPassword) {

        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new CustomException("Invalid reset token", 400));

        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new CustomException("Token expired", 401);
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);

        userRepository.save(user);
    }

    @Override
    public AuthResponse googleLogin(String idToken) {

        var payload = googleOAuthService.verifyToken(idToken);

        String email = payload.getEmail();

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(email)
                            .provider("GOOGLE")
                            .role("USER")
                            .emailVerified(true)
                            .createdAt(LocalDateTime.now())
                            .build();

                    return userRepository.save(newUser);
                });

        String accessToken = jwtProvider.generateToken(user.getId());
        String refreshToken = tokenService.createRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}