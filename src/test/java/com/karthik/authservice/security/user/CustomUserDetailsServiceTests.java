package com.karthik.authservice.security.user;

import com.karthik.authservice.entity.User;
import com.karthik.authservice.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTests {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService service;

    @Test
    void loadUserByUsername_shouldReturnUserDetails_whenUserExists() {

        User user = User.builder()
                .id("user-id")
                .email("test@gmail.com")
                .password("pass")
                .build();

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(user));

        var result = service.loadUserByUsername("test@gmail.com");

        assertNotNull(result);
        assertEquals("test@gmail.com", result.getUsername());
    }

    @Test
    void loadUserByUsername_shouldThrowException_whenUserNotFound() {

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
                service.loadUserByUsername("test@gmail.com"));
    }

    @Test
    void loadUserById_shouldReturnUserDetails_whenUserExists() {

        User user = User.builder()
                .id("user-id")
                .email("test@gmail.com")
                .build();

        when(userRepository.findById("user-id"))
                .thenReturn(Optional.of(user));

        var result = service.loadUserById("user-id");

        assertNotNull(result);
        assertEquals("test@gmail.com", result.getUsername());
    }

    @Test
    void loadUserById_shouldThrowException_whenUserNotFound() {

        when(userRepository.findById("user-id"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                service.loadUserById("user-id"));
    }


}