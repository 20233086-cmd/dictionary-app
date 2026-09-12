package com.dictionary.app.repository;

import com.dictionary.app.entity.ChatConversation;
import com.dictionary.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatConversationRepository extends JpaRepository<ChatConversation, Long> {
    List<ChatConversation> findByUserOrderByCreatedAtDesc(User user);
    Optional<ChatConversation> findByIdAndUser(Long id, User user);
}
