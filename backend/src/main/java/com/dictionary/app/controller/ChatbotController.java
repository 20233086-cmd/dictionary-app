package com.dictionary.app.controller;

import com.dictionary.app.dto.request.ChatRequest;
import com.dictionary.app.dto.response.ApiResponse;
import com.dictionary.app.dto.response.ChatConversationResponse;
import com.dictionary.app.dto.response.ChatMessageResponse;
import com.dictionary.app.dto.response.ChatResponse;
import com.dictionary.app.service.ChatbotService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping("/message")
    public ApiResponse<ChatResponse> sendMessage(@Valid @RequestBody ChatRequest request) {
        return ApiResponse.ok(chatbotService.sendMessage(request));
    }

    @GetMapping("/conversations")
    public ApiResponse<List<ChatConversationResponse>> conversations() {
        List<ChatConversationResponse> list = chatbotService.getMyConversations()
                .stream().map(ChatConversationResponse::fromEntity).collect(Collectors.toList());
        return ApiResponse.ok(list);
    }

    @GetMapping("/conversations/{id}/messages")
    public ApiResponse<List<ChatMessageResponse>> messages(@PathVariable Long id) {
        List<ChatMessageResponse> list = chatbotService.getMessages(id)
                .stream().map(ChatMessageResponse::fromEntity).collect(Collectors.toList());
        return ApiResponse.ok(list);
    }
}
