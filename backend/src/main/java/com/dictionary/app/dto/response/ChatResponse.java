package com.dictionary.app.dto.response;

public class ChatResponse {
    private Long conversationId;
    private String mode;
    private String reply;

    public ChatResponse() {
    }

    public ChatResponse(Long conversationId, String mode, String reply) {
        this.conversationId = conversationId;
        this.mode = mode;
        this.reply = reply;
    }

    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }
}
