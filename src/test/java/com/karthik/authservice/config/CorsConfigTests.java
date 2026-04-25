package com.karthik.authservice.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.junit.jupiter.api.Assertions.*;

class CorsConfigTests {

    @Test
    void corsConfigurer_shouldNotBeNull() {

        CorsConfig config = new CorsConfig();

        WebMvcConfigurer cors = config.corsConfigurer();

        assertNotNull(cors);
    }
}