package com.dictionary.app.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    @JsonIgnore
    private ChatConversation conversation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Sender sender;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    public ChatMessage() {
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ChatConversation getConversation() { return conversation; }
    public void setConversation(ChatConversation conversation) { this.conversation = conversation; }

    public Sender getSender() { return sender; }
    public void setSender(Sender sender) { this.sender = sender; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public enum Sender {
        USER, BOT
    }
}
