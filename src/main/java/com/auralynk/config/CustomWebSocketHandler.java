package com.auralynk.config;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomWebSocketHandler extends WebSocketHandlerDecorator {
    
    public CustomWebSocketHandler(WebSocketHandler delegate) {
        super(delegate);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("New WebSocket connection established: {}", session.getId());
        super.afterConnectionEstablished(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        log.info("WebSocket connection closed: {} with status: {}", session.getId(), closeStatus);
        super.afterConnectionClosed(session, closeStatus);
    }
} 