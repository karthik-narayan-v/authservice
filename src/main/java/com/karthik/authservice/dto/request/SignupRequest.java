package com.karthik.authservice.dto.request;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class SignupRequest {

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
}