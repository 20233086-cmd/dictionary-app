package com.dictionary.app.dto.response;

import com.dictionary.app.entity.ChatMessage;

import java.time.LocalDateTime;

public class ChatMessageResponse {
    private Long id;
    private String sender;
    private String content;
    private LocalDateTime createdAt;

    public ChatMessageResponse() {
    }

    public ChatMessageResponse(Long id, String sender, String content, LocalDateTime createdAt) {
        this.id = id;
        this.sender = sender;
        this.content = content;
        this.createdAt = createdAt;
    }

    public static ChatMessageResponse fromEntity(ChatMessage m) {
        return new ChatMessageResponse(m.getId(), m.getSender().name(), m.getContent(), m.getCreatedAt());
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
