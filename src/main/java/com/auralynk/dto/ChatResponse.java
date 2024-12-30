package com.auralynk.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatResponse {
    private String message;
    private LocalDateTime timestamp;
    private String sessionId;
} 