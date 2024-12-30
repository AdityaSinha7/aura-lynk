package com.auralynk.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableWebSocketMessageBroker
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        log.info("Registering STOMP endpoints...");
        registry.addEndpoint("/ws/chat")
            .setAllowedOrigins("http://localhost:5173")
            .withSockJS()
            .setWebSocketEnabled(true)
            .setHeartbeatTime(25000)
            .setDisconnectDelay(30000);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        log.info("Configuring message broker...");
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setMessageSizeLimit(64 * 1024)
            .setSendBufferSizeLimit(512 * 1024)
            .setSendTimeLimit(20000);
        registration.addDecoratorFactory(new CustomWebSocketHandlerDecoratorFactory());
    }
} 