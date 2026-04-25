package com.karthik.authservice.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // Dummy controller to trigger exceptions
    @RestController
    static class TestController {

        @GetMapping("/custom")
        public String custom() {
            throw new CustomException("Custom error", 400);
        }

        @GetMapping("/generic")
        public String generic() {
            throw new RuntimeException("Something went wrong");
        }

        @PostMapping("/validate")
        public String validate(@Valid @RequestBody TestRequest request) {
            return "OK";
        }
    }

    // DTO for validation test
    static class TestRequest {
        @NotBlank(message = "Name is required")
        public String name;
    }

    @Test
    void shouldHandleCustomException() throws Exception {

        mockMvc.perform(get("/custom"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Custom error"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldHandleGenericException() throws Exception {

        mockMvc.perform(get("/generic"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Something went wrong"))
                .andExpect(jsonPath("$.status").value(500));
    }

    @Test
    void shouldHandleValidationException() throws Exception {

        String request = "{}"; // missing 'name'

        mockMvc.perform(post("/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Name is required"))
                .andExpect(jsonPath("$.status").value(400));
    }
}