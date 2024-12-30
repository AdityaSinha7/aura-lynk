package com.auralynk.repository;

import com.auralynk.model.ChatSession;
import com.auralynk.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    Page<ChatSession> findByUser(User user, Pageable pageable);
} 