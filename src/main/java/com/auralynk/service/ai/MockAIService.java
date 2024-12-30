package com.auralynk.service.ai;

import com.auralynk.dto.ChatMessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Slf4j
public class MockAIService implements AIChatService {
    private static final String DEFAULT_MODEL = "mock-model";

    @Override
    public String generateResponse(List<ChatMessageDTO> messages, String model) {
        // Get the last user message
        String lastUserMessage = messages.stream()
            .filter(msg -> "user".equals(msg.getRole()))
            .reduce((first, second) -> second)
            .map(ChatMessageDTO::getContent)
            .orElse("");

        // Generate a simple response
        return "Hello! I understand you said: '" + lastUserMessage + 
               "'. I'm a mock AI service for testing purposes. " +
               "How can I help you further?";
    }

    @Override
    public String getDefaultModel() {
        return DEFAULT_MODEL;
    }
} 