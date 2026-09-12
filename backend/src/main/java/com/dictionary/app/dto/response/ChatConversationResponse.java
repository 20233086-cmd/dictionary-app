package com.dictionary.app.dto.response;

import com.dictionary.app.entity.ChatConversation;

import java.time.LocalDateTime;

public class ChatConversationResponse {
    private Long id;
    private String title;
    private String mode;
    private LocalDateTime createdAt;

    public ChatConversationResponse() {
    }

    public ChatConversationResponse(Long id, String title, String mode, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.mode = mode;
        this.createdAt = createdAt;
    }

    public static ChatConversationResponse fromEntity(ChatConversation c) {
        return new ChatConversationResponse(c.getId(), c.getTitle(), c.getMode(), c.getCreatedAt());
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
