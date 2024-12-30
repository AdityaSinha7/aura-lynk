package com.auralynk.config;

import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory;

public class CustomWebSocketHandlerDecoratorFactory implements WebSocketHandlerDecoratorFactory {
    @Override
    public WebSocketHandler decorate(WebSocketHandler handler) {
        return new CustomWebSocketHandler(handler);
    }
} 