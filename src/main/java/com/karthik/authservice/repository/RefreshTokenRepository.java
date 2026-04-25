package com.karthik.authservice.repository;

import com.karthik.authservice.entity.RefreshToken;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<@NonNull RefreshToken, @NonNull String> {
    Optional<RefreshToken> findByToken(String token);
}