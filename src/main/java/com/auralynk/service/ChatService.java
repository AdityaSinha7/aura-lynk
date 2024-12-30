package com.auralynk.service;

import com.auralynk.model.ChatMessage;
import com.auralynk.model.ChatSession;
import com.auralynk.model.User;
import com.auralynk.repository.ChatMessageRepository;
import com.auralynk.repository.ChatSessionRepository;
import com.auralynk.service.ai.AIChatService;
import com.auralynk.dto.ChatMessageDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.auralynk.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {
    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;
    private final AIChatService aiChatService;

    public ChatSession createSession(User user, String sessionName) {
        ChatSession session = new ChatSession();
        session.setUser(user);
        session.setSessionName(sessionName);
        session.setCreatedAt(LocalDateTime.now());
        session.setLastMessageAt(LocalDateTime.now());
        session.setAiModel(aiChatService.getDefaultModel());
        
        return sessionRepository.save(session);
    }

    public ChatMessage processMessage(ChatSession session, String userMessage) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            throw new IllegalArgumentException("Message cannot be empty");
        }

        log.debug("Processing message for session: {}", session.getId());

        // Save user message
        ChatMessage userChatMessage = saveMessage(session, "user", userMessage);
        
        try {
            // Get conversation history
            List<ChatMessageDTO> messages = buildConversationHistory(session);
            
            log.debug("Built conversation history with {} messages", messages.size());

            // Add user's new message
            messages.add(new ChatMessageDTO("user", userMessage));

            log.debug("Sending request to OpenAI");
            // Get AI response
            String aiResponse = aiChatService.generateResponse(messages, session.getAiModel());

            log.debug("Received response from OpenAI");
            // Save AI response
            return saveMessage(session, "assistant", aiResponse);
        } catch (Exception e) {
            log.error("Error processing message with AI: ", e);
            throw new RuntimeException("Failed to process message: " + e.getMessage(), e);
        }
    }

    private ChatMessage saveMessage(ChatSession session, String role, String content) {
        ChatMessage message = new ChatMessage();
        message.setSession(session);
        message.setRole(role);
        message.setContent(content);
        message.setTimestamp(LocalDateTime.now());
        
        session.setLastMessageAt(LocalDateTime.now());
        sessionRepository.save(session);
        
        return messageRepository.save(message);
    }

    private List<ChatMessageDTO> buildConversationHistory(ChatSession session) {
        // Get last 50 messages for context
        Page<ChatMessage> historyPage = messageRepository.findBySessionOrderByTimestampDesc(
            session,
            PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "timestamp"))
        );
        List<ChatMessage> history = new ArrayList<>(historyPage.getContent());
        Collections.reverse(history); // Now safe to reverse the ArrayList
        List<ChatMessageDTO> messages = new ArrayList<>();
        
        // Add system message for context
        messages.add(new ChatMessageDTO(
            "system",
            "You are AuraLynk, an AI girlfriend chatbot. Be friendly, caring, and engaging while maintaining appropriate boundaries."
        ));

        // Add conversation history
        for (ChatMessage msg : history) {
            messages.add(new ChatMessageDTO(msg.getRole(), msg.getContent()));
        }

        return messages;
    }

    public ChatSession getSessionById(Long sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found with id: " + sessionId));
    }

    public Page<ChatSession> getSessionsByUser(User user, Pageable pageable) {
        return sessionRepository.findByUser(user, pageable);
    }

    public Page<ChatMessage> getMessagesBySession(ChatSession session, Pageable pageable) {
        return messageRepository.findBySessionOrderByTimestampDesc(session, pageable);
    }

    public void deleteSession(ChatSession session) {
        // Delete all messages first due to foreign key constraint
        List<ChatMessage> messages = messageRepository.findBySessionOrderByTimestamp(session);
        messageRepository.deleteAll(messages);
        sessionRepository.delete(session);
    }

    public ChatSession updateSession(ChatSession session) {
        return sessionRepository.save(session);
    }

    public ChatMessage sendMessage(ChatSession session, String message) {
        // Create user message
        ChatMessage userMessage = new ChatMessage();
        userMessage.setSession(session);
        userMessage.setContent(message);
        userMessage.setRole("user");
        userMessage.setTimestamp(LocalDateTime.now());
        messageRepository.save(userMessage);

        // Get AI response
        ChatMessage aiResponse = processMessage(session, message);
        
        // Update session's last message time
        session.setLastMessageAt(LocalDateTime.now());
        sessionRepository.save(session);

        return aiResponse;
    }
} 