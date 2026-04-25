package com.karthik.authservice.security.ratelimit;

import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RateLimitFilterTests {

    @Mock
    private RateLimiterService rateLimiterService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private RateLimitFilter rateLimitFilter;

    @Test
    void doFilter_shouldAllowRequest_whenWithinLimit() throws Exception {

        Bucket bucket = mock(Bucket.class);

        when(request.getRequestURI()).thenReturn("/auth/login");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(rateLimiterService.resolveBucket("127.0.0.1")).thenReturn(bucket);
        when(bucket.tryConsume(1)).thenReturn(true);

        rateLimitFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(429);
    }

    @Test
    void doFilter_shouldBlockRequest_whenLimitExceeded() throws Exception {

        Bucket bucket = mock(Bucket.class);

        when(request.getRequestURI()).thenReturn("/auth/login");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(rateLimiterService.resolveBucket("127.0.0.1")).thenReturn(bucket);
        when(bucket.tryConsume(1)).thenReturn(false);

        StringWriter writer = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(writer));

        rateLimitFilter.doFilter(request, response, filterChain);

        verify(response).setStatus(429);
        assertTrue(writer.toString().contains("Too many requests"));

        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilter_shouldSkipRateLimit_forNonAuthEndpoints() throws Exception {

        when(request.getRequestURI()).thenReturn("/users/me");

        rateLimitFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(rateLimiterService);
    }
}