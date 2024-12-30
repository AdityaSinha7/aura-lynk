package com.auralynk.service.ai;

import com.auralynk.dto.ChatMessageDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.auralynk.config.AIPersonalityConfig;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

@Service
@Slf4j
public class HuggingFaceService implements AIChatService {
    private final String apiToken;
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;
    private final AIPersonalityConfig aiPersonalityConfig;
    private final String systemContext;
    private final String defaultModel;
    private final String fallbackModel;

    private static final String SYSTEM_PREFIX = "System: ";
    private static final String USER_PREFIX = "User: ";
    private static final String AI_PREFIX = "AI: ";

    public HuggingFaceService(
            @Value("${huggingface.api.token}") String apiToken,
            @Value("${huggingface.model.default}") String defaultModel,
            @Value("${huggingface.model.fallback}") String fallbackModel,
            AIPersonalityConfig aiPersonalityConfig) {
        this.apiToken = apiToken;
        this.client = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
        this.aiPersonalityConfig = aiPersonalityConfig;
        this.systemContext = "System: " + aiPersonalityConfig.getSystemContext() + "\n\n";
        this.defaultModel = defaultModel;
        this.fallbackModel = fallbackModel;
    }

    private String getModelEndpoint(String requestedModel) {
        String modelToUse = requestedModel != null ? requestedModel : defaultModel;
        return String.format("https://api-inference.huggingface.co/models/%s", modelToUse);
    }

    @Override
    public String generateResponse(List<ChatMessageDTO> messages, String model) {
        StringBuilder conversation = new StringBuilder();
        if (messages.size() <= 1) {
            conversation.append(systemContext).append("\n");
        }

        for (ChatMessageDTO msg : messages) {
            conversation.append(switch (msg.getRole()) {
                case "system" -> SYSTEM_PREFIX;
                case "user" -> USER_PREFIX;
                case "assistant" -> AI_PREFIX;
                default -> throw new IllegalArgumentException("Unknown role: " + msg.getRole());
            })
            .append(msg.getContent())
            .append("\n");
        }

        log.debug("Conversation history: {}", conversation.toString());
        try {
            String url = getModelEndpoint(model);
            Map<String, Object> payload = new HashMap<>();
            payload.put("inputs", conversation.toString());

            // Add generation parameters
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("do_sample", true);
            parameters.put("max_length", 100);
            parameters.put("top_p", 0.9);
            payload.put("parameters", parameters);

            String jsonPayload = objectMapper.writeValueAsString(payload);
            log.debug("Sending request to HuggingFace API: {}", jsonPayload);

            RequestBody body = RequestBody.create(
                jsonPayload,
                MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Authorization", "Bearer " + apiToken)
                .addHeader("Content-Type", "application/json")
                .build();

            int maxRetries = 3;
            int retryCount = 0;
            while (retryCount < maxRetries) {
                try (Response response = client.newCall(request).execute()) {
                    if (response.code() == 503) {
                        log.info("Model is loading, attempt {} of {}", retryCount + 1, maxRetries);
                        Thread.sleep(2000);
                        retryCount++;
                        continue;
                    }
                    
                    if (!response.isSuccessful()) {
                        String errorBody = response.body().string();
                        log.error("HuggingFace API error: {} - {} - {}", 
                            response.code(), response.message(), errorBody);
                        throw new RuntimeException("HuggingFace API error: " + response.code());
                    }

                    String responseBody = response.body().string();
                    log.debug("HuggingFace API response: {}", responseBody);
                    List<Map<String, String>> responses = objectMapper.readValue(responseBody, List.class);
                    String fullResponse = responses.get(0).get("generated_text");
                    // Extract everything after the last "User:" or "AI:" prompt
                    // String[] parts = fullResponse.split("(?:User:|AI:)");
                    // log.info("parts: " + Arrays.toString(parts));
                    return fullResponse;
                }
            }
            throw new RuntimeException("Failed to get response after " + maxRetries + " retries");
        } catch (IOException e) {
            log.error("Error calling HuggingFace API", e);
            throw new RuntimeException("Failed to get response from HuggingFace", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Operation interrupted", e);
        } catch (Exception e) {
            log.warn("Error with primary model, trying fallback model", e);
            if (!getModelEndpoint(model).equals(getModelEndpoint(fallbackModel))) {
                return generateResponse(messages, fallbackModel);
            }
            throw e;
        }
    }

    @Override
    public String getDefaultModel() {
        return defaultModel;
    }
} 