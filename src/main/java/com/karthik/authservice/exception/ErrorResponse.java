package com.karthik.authservice.exception;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.time.LocalDateTime;

@Data
@Builder
public class ErrorResponse {

    private int status;

    @NonNull
    private String message;

    @NonNull
    private LocalDateTime timestamp;
}