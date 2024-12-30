package com.auralynk.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AIPersonalityConfig {
    @Value("${ai.personality.context}")
    private String personalityContext;

    public String getSystemContext() {
        return personalityContext;
    }
} 