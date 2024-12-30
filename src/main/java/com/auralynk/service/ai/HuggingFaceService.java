package com.auralynk.service.ai;

import com.auralynk.dto.ChatMessageDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
@Slf4j
public class HuggingFaceService implements AIChatService {
    private static final String DEFAULT_MODEL = "microsoft/DialoGPT-medium";
    private final String apiToken;
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;

    public HuggingFaceService(
            @Value("${huggingface.api.token}") String apiToken) {
        this.apiToken = apiToken;
        this.client = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String generateResponse(List<ChatMessageDTO> messages, String model) {
        String userMessage = messages.stream()
                .filter(msg -> "user".equals(msg.getRole()))
                .reduce((first, second) -> second)
                .map(ChatMessageDTO::getContent)
                .orElse("");

        try {
            String modelEndpoint = model != null ? model : DEFAULT_MODEL;
            String url = String.format("https://api-inference.huggingface.co/models/%s", modelEndpoint);

            Map<String, Object> payload = new HashMap<>();
            payload.put("inputs", userMessage);

            RequestBody body = RequestBody.create(
                objectMapper.writeValueAsString(payload),
                MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Authorization", "Bearer " + apiToken)
                .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new RuntimeException("HuggingFace API error: " + response.code());
                }

                String responseBody = response.body().string();
                List<String> responses = objectMapper.readValue(responseBody, List.class);
                return responses.get(0);
            }
        } catch (IOException e) {
            log.error("Error calling HuggingFace API", e);
            throw new RuntimeException("Failed to get response from HuggingFace", e);
        }
    }

    @Override
    public String getDefaultModel() {
        return DEFAULT_MODEL;
    }
} 