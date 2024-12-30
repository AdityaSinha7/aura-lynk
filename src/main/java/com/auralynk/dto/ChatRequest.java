package com.auralynk.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatRequest {
    @NotNull(message = "Session ID is required")
    private Long sessionId;

    @NotBlank(message = "Message cannot be empty")
    private String message;
} 