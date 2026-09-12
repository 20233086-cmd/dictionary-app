package com.dictionary.app.dto.request;

import jakarta.validation.constraints.NotBlank;

public class ChatRequest {
    private Long conversationId; // null => tạo cuộc trò chuyện mới

    /** Chỉ áp dụng khi tạo cuộc trò chuyện mới: "QA" (mặc định) hoặc "PRACTICE" (luyện hội thoại) */
    private String mode;

    @NotBlank(message = "Nội dung không được để trống")
    private String message;

    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
