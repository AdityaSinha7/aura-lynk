package com.auralynk.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChatMessageDTO {
    private String role;
    private String content;
} 