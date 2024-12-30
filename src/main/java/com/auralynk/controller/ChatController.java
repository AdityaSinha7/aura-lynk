package com.auralynk.controller;

import com.auralynk.dto.ChatRequest;
import com.auralynk.dto.ChatResponse;
import com.auralynk.dto.PageResponse;
import com.auralynk.model.ChatMessage;
import com.auralynk.model.ChatSession;
import com.auralynk.model.User;
import com.auralynk.service.ChatService;
import com.auralynk.service.UserService;
import com.auralynk.util.SortingUtil;
import com.auralynk.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {
    private final ChatService chatService;
    private final UserService userService;

    @PostMapping("/sessions")
    public ResponseEntity<ChatSession> createSession(
            Authentication authentication,
            @RequestBody Map<String, String> request) {
        if (!request.containsKey("sessionName") || request.get("sessionName").trim().isEmpty()) {
            throw new IllegalArgumentException("Session name is required");
        }

        User user = userService.findByEmail(authentication.getName());
        ChatSession session = chatService.createSession(user, request.get("sessionName"));
        return ResponseEntity.ok(session);
    }

    @GetMapping("/sessions")
    public ResponseEntity<PageResponse<ChatSession>> getSessions(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "lastMessageAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        log.debug("Getting sessions for user: {}", authentication.getName());
        try {
            User user = userService.findByEmail(authentication.getName());
            Sort sort = SortingUtil.validateAndGetSessionSort(sortBy, direction);
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<ChatSession> sessions = chatService.getSessionsByUser(user, pageable);
            log.debug("Found {} sessions", sessions.getTotalElements());
            return ResponseEntity.ok(new PageResponse<>(sessions));
        } catch (Exception e) {
            log.error("Error getting sessions", e);
            throw e;
        }
    }

    @PostMapping("/send")
    public ResponseEntity<ChatMessage> sendMessage(@RequestBody ChatRequest request, Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        ChatSession session = chatService.getSessionById(request.getSessionId());
        
        // Verify user owns this session
        if (!session.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You don't have permission to access this chat session");
        }

        ChatMessage response = chatService.sendMessage(session, request.getMessage());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<PageResponse<ChatMessage>> getMessages(
            @PathVariable Long sessionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        ChatSession session = chatService.getSessionById(sessionId);

        if (!session.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You don't have permission to access this chat session");
        }

        Sort sort = SortingUtil.validateAndGetMessageSort(sortBy, direction);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ChatMessage> messages = chatService.getMessagesBySession(session, pageable);
        return ResponseEntity.ok(new PageResponse<>(messages));
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> deleteSession(
            Authentication authentication,
            @PathVariable Long sessionId) {
        User user = userService.findByEmail(authentication.getName());
        ChatSession session = chatService.getSessionById(sessionId);

        // Verify user owns this session
        if (!session.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You don't have permission to access this chat session");
        }

        chatService.deleteSession(session);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/sessions/{sessionId}")
    public ResponseEntity<ChatSession> updateSession(
            Authentication authentication,
            @PathVariable Long sessionId,
            @RequestBody Map<String, String> request) {
        if (!request.containsKey("sessionName") || request.get("sessionName").trim().isEmpty()) {
            throw new IllegalArgumentException("Session name is required");
        }

        User user = userService.findByEmail(authentication.getName());
        ChatSession session = chatService.getSessionById(sessionId);

        if (!session.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You don't have permission to access this chat session");
        }

        session.setSessionName(request.get("sessionName"));
        session = chatService.updateSession(session);
        return ResponseEntity.ok(session);
    }
} 