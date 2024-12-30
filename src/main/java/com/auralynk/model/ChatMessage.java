package com.auralynk.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "chat_messages")
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "session_id", nullable = false)
    private ChatSession session;

    @Column(nullable = false)
    private String role; // 'user' or 'assistant'

    @Column(columnDefinition = "text", nullable = false)
    private String content;

    @Column(nullable = false)
    private LocalDateTime timestamp;
} 