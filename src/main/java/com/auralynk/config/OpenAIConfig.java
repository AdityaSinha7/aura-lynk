package com.auralynk.config;

import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAIConfig {
    
    @Value("${openai.api.key}")
    private String openaiApiKey;
    @Value("${openai.model:gpt-3.5-turbo}")
    private String defaultModel;

    @Bean
    public OpenAiService openAiService() {
        if (openaiApiKey == null || openaiApiKey.trim().isEmpty()) {
            throw new IllegalStateException("OpenAI API key is not configured");
        }
        return new OpenAiService(openaiApiKey);
    }
} 