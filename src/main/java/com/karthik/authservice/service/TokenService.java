package com.karthik.authservice.service;

import com.karthik.authservice.entity.User;

public interface TokenService {
    String createRefreshToken(User user);
    String validateRefreshToken(String token);
    void deleteRefreshToken(String token);
}