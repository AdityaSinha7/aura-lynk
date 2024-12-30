package com.auralynk.config;

import com.auralynk.service.ai.AIChatService;
import com.auralynk.service.ai.HuggingFaceService;
import com.auralynk.service.ai.MockAIService;
import com.auralynk.service.ai.OpenAIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@Slf4j
public class AIConfig {

    @Value("${ai.service.type:mock}")
    private String aiServiceType;

    @Bean
    @Primary
    public AIChatService aiChatService(
            MockAIService mockService,
            OpenAIService openAIService,
            HuggingFaceService huggingFaceService) {
        
        log.info("Configuring AI service with type: {}", aiServiceType);

        return switch (aiServiceType.toLowerCase().trim()) {
            case "openai" -> openAIService;
            case "huggingface" -> huggingFaceService;
            default -> {
                log.warn("Unknown service type '{}', falling back to mock service", aiServiceType);
                yield mockService;
            }
        };
    }
} 