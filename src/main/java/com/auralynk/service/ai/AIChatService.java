package com.auralynk.service.ai;

import com.auralynk.dto.ChatMessageDTO;
import java.util.List;

public interface AIChatService {
    String generateResponse(List<ChatMessageDTO> messages, String model);
    String getDefaultModel();
} 