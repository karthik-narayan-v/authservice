package com.karthik.authservice.controller;

import com.karthik.authservice.security.user.CustomUserDetails;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(new UserController()).build();
    }

    @Test
    void getProfile_shouldReturnUserEmail() throws Exception {

        CustomUserDetails userDetails = new CustomUserDetails(
                com.karthik.authservice.entity.User.builder()
                        .email("test@gmail.com")
                        .build()
        );

        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );

        mockMvc.perform(get("/users/me")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(content().string("User Email: test@gmail.com"));
    }

    @Test
    void getProfile_shouldHandleNullUser() throws Exception {

        Authentication auth = new UsernamePasswordAuthenticationToken(
                null,
                null
        );

        mockMvc.perform(get("/users/me")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(content().string("User Email: null"));
    }

    @Test
    void adminOnly_shouldReturnMessage() throws Exception {

        mockMvc.perform(get("/users/admin"))
                .andExpect(status().isOk())
                .andExpect(content().string("Only ADMIN can access this"));
    }

}