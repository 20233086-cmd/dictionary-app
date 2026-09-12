package com.dictionary.app.repository;

import com.dictionary.app.entity.ChatConversation;
import com.dictionary.app.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByConversationOrderByCreatedAtAsc(ChatConversation conversation);
}
