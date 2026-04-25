package com.karthik.authservice.security.jwt;

import com.karthik.authservice.security.user.CustomUserDetails;
import com.karthik.authservice.security.user.CustomUserDetailsService;

import jakarta.servlet.FilterChain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtFilterTests {

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtFilter jwtFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_shouldSetAuthentication_whenTokenIsValid() throws Exception {

        String token = "valid-token";

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer " + token);

        when(jwtProvider.validateToken(token)).thenReturn(true);
        when(jwtProvider.getUserIdFromToken(token)).thenReturn("user-id");

        CustomUserDetails userDetails = mock(CustomUserDetails.class);

        when(userDetailsService.loadUserById("user-id"))
                .thenReturn(userDetails);

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_shouldNotSetAuthentication_whenTokenIsInvalid() throws Exception {

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer invalid-token");

        when(jwtProvider.validateToken("invalid-token")).thenReturn(false);

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_shouldSkip_whenNoAuthorizationHeader() throws Exception {

        when(request.getHeader("Authorization")).thenReturn(null);

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_shouldSkip_whenHeaderDoesNotStartWithBearer() throws Exception {

        when(request.getHeader("Authorization"))
                .thenReturn("Basic abc123");

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(filterChain).doFilter(request, response);
    }
}