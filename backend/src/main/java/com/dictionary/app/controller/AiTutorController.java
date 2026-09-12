package com.dictionary.app.controller;

import com.dictionary.app.dto.request.AiChatRequest;
import com.dictionary.app.dto.response.ApiResponse;
import com.dictionary.app.service.GeminiClient;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai/tutor")
public class AiTutorController {
    private final GeminiClient geminiClient;

    public AiTutorController(GeminiClient geminiClient) {
        this.geminiClient = geminiClient;
    }

    @PostMapping("/chat")
    public ApiResponse<String> chat(@Valid @RequestBody AiChatRequest request) {
        return ApiResponse.ok(geminiClient.chat(
                List.of(GeminiClient.ChatTurn.user(request.getMessage().trim())), "TUTOR"));
    }

    @PostMapping("/correct")
    public ApiResponse<String> correct(@Valid @RequestBody AiChatRequest request) {
        return ApiResponse.ok(geminiClient.correctSentence(request.getMessage().trim()));
    }
}
