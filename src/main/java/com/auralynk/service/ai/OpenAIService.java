package com.auralynk.service.ai;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import com.auralynk.dto.ChatMessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OpenAIService implements AIChatService {
    private final OpenAiService openAiService;
    private static final String DEFAULT_MODEL = "gpt-3.5-turbo";

    public OpenAIService(OpenAiService openAiService) {
        this.openAiService = openAiService;
    }

    @Override
    public String generateResponse(List<ChatMessageDTO> messages, String model) {
        List<ChatMessage> openAiMessages = messages.stream()
            .map(msg -> new ChatMessage(msg.getRole(), msg.getContent()))
            .collect(Collectors.toList());

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