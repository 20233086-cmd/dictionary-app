package com.dictionary.app.controller;

import com.dictionary.app.dto.request.ReviewRequest;
import com.dictionary.app.dto.response.ApiResponse;
import com.dictionary.app.dto.response.FlashcardResponse;
import com.dictionary.app.service.FlashcardService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/flashcards")
public class FlashcardController {

    private final FlashcardService flashcardService;

    public FlashcardController(FlashcardService flashcardService) {
        this.flashcardService = flashcardService;
    }

    @PostMapping("/{wordId}")
    public ApiResponse<Void> addToDeck(@PathVariable Long wordId) {
        flashcardService.addToDeck(wordId);
        return ApiResponse.ok("Đã thêm vào bộ ôn tập", null);
    }

    @DeleteMapping("/{wordId}")
    public ApiResponse<Void> removeFromDeck(@PathVariable Long wordId) {
        flashcardService.removeFromDeck(wordId);
        return ApiResponse.ok("Đã bỏ khỏi bộ ôn tập", null);
    }

    @GetMapping("/{wordId}/check")
    public ApiResponse<Boolean> isInDeck(@PathVariable Long wordId) {
        return ApiResponse.ok(flashcardService.isInDeck(wordId));
    }

    @GetMapping("/due")
    public ApiResponse<List<FlashcardResponse>> due(@RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.ok(flashcardService.getDueCards(limit));
    }

    @GetMapping("/deck")
    public ApiResponse<List<FlashcardResponse>> deck() {
        return ApiResponse.ok(flashcardService.getFullDeck());
    }

    @GetMapping("/summary")
    public ApiResponse<Map<String, Long>> summary() {
        return ApiResponse.ok(Map.of(
                "dueToday", flashcardService.countDueToday(),
                "totalCards", flashcardService.countDeckSize()
        ));
    }

    @PostMapping("/review/{wordId}")
    public ApiResponse<FlashcardResponse> review(@PathVariable Long wordId, @Valid @RequestBody ReviewRequest request) {
        return ApiResponse.ok(flashcardService.review(wordId, request.getQuality()));
    }
}
