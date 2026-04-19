package com.karthik.authservice.repository;

import com.karthik.authservice.entity.RefreshToken;
import com.karthik.authservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUserId(String userId);
    void deleteByUser(User user);
}