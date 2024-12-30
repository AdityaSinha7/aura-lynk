package com.auralynk.repository;

import com.auralynk.model.ChatMessage;
import com.auralynk.model.ChatSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    Page<ChatMessage> findBySessionOrderByTimestampAsc(ChatSession session, Pageable pageable);
    List<ChatMessage> findBySessionOrderByTimestamp(ChatSession session);
} 