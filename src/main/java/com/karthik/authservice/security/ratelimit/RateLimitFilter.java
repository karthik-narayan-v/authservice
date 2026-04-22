package com.karthik.authservice.security.ratelimit;

import io.github.bucket4j.Bucket;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RateLimitFilter implements Filter {

    private final RateLimiterService rateLimiterService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();

        // Apply only on auth endpoints
        if (path.startsWith("/auth/login") || path.startsWith("/auth/signup")) {

            String ip = httpRequest.getRemoteAddr();

            Bucket bucket = rateLimiterService.resolveBucket(ip);

            if (!bucket.tryConsume(1)) {
                httpResponse.setStatus(429);
                httpResponse.getWriter().write("Too many requests. Try again later.");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}