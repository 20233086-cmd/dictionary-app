package com.dictionary.app.controller;

import com.dictionary.app.dto.request.CorrectionRequest;
import com.dictionary.app.dto.response.ApiResponse;
import com.dictionary.app.service.GeminiClient;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {
    private final GeminiClient geminiClient;
    public AiController(GeminiClient geminiClient) { this.geminiClient = geminiClient; }

    @PostMapping("/correct")
    public ApiResponse<String> correct(@Valid @RequestBody CorrectionRequest request) {
        return ApiResponse.ok(geminiClient.correctSentence(request.getSentence().trim()));
    }
}
