package com.auralynk.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class WebSocketChatController {

    @MessageMapping("/chat/{sessionId}")
    @SendTo("/topic/chat/{sessionId}")
    public String handleMessage(String message) {
        log.info("Received message: {}", message);
        return message;
    }

    @MessageMapping("/test")
    @SendTo("/topic/test")
    public String test(String message) {
        log.info("Test endpoint hit with message: {}", message);
        return "Server received: " + message;
    }

    @MessageMapping("/connect")
    @SendTo("/topic/connect")
    public String handleConnect() {
        log.info("Client connected to websocket");
        return "Connected successfully!";
    }
} 