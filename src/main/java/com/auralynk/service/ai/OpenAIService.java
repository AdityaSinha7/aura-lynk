package com.auralynk.service.ai;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import com.auralynk.dto.ChatMessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import com.auralynk.config.AIPersonalityConfig;

@Service
@Slf4j
public class OpenAIService implements AIChatService {
    private final OpenAiService openAiService;
    private static final String DEFAULT_MODEL = "gpt-3.5-turbo";
    private final String systemContext;
    private final AIPersonalityConfig aiPersonalityConfig;

    public OpenAIService(OpenAiService openAiService, AIPersonalityConfig aiPersonalityConfig) {
        this.openAiService = openAiService;
        this.aiPersonalityConfig = aiPersonalityConfig;
        this.systemContext = aiPersonalityConfig.getSystemContext();
    }

    @Override
    public String generateResponse(List<ChatMessageDTO> messages, String model) {
        List<ChatMessage> openAiMessages = new ArrayList<>();
        
        // Add system message only for new conversations
        if (messages.size() <= 1) {
            openAiMessages.add(new ChatMessage("system", systemContext));
        }
        
        // Convert messages to OpenAI format
        openAiMessages.addAll(messages.stream()
            .map(msg -> new ChatMessage(msg.getRole(), msg.getContent()))
            .collect(Collectors.toList()));

        ChatCompletionRequest request = ChatCompletionRequest.builder()
            .model(model != null ? model : DEFAULT_MODEL)
            .messages(openAiMessages)
            .build();

        return openAiService.createChatCompletion(request)
            .getChoices().get(0).getMessage().getContent();
    }

    @Override
    public String getDefaultModel() {
        return DEFAULT_MODEL;
    }
} 