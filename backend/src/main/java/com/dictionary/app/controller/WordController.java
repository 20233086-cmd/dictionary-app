package com.dictionary.app.controller;

import com.dictionary.app.dto.response.ApiResponse;
import com.dictionary.app.dto.response.PageResponse;
import com.dictionary.app.dto.response.WordResponse;
import com.dictionary.app.service.SearchHistoryService;
import com.dictionary.app.service.WordService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/words")
public class WordController {

    private final WordService wordService;
    private final SearchHistoryService searchHistoryService;

    public WordController(WordService wordService, SearchHistoryService searchHistoryService) {
        this.wordService = wordService;
        this.searchHistoryService = searchHistoryService;
    }

    @GetMapping("/lookup/{english}")
    public ApiResponse<WordResponse> lookup(@PathVariable String english) {
        WordResponse result = wordService.lookup(english);
        searchHistoryService.record(english.trim());
        return ApiResponse.ok(result);
    }

    @GetMapping("/{id}")
    public ApiResponse<WordResponse> getById(@PathVariable Long id) {
        return ApiResponse.ok(wordService.getById(id));
    }

    @GetMapping("/search")
    public ApiResponse<PageResponse<WordResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(PageResponse.from(wordService.search(keyword, page, size)));
    }

    @GetMapping("/reverse-search")
    public ApiResponse<PageResponse<WordResponse>> reverseSearch(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(PageResponse.from(wordService.reverseSearch(keyword, page, size)));
    }

    @GetMapping("/suggest")
    public ApiResponse<List<String>> suggest(@RequestParam String prefix) {
        return ApiResponse.ok(wordService.suggest(prefix));
    }

    @GetMapping("/word-of-day")
    public ApiResponse<WordResponse> wordOfTheDay() {
        return ApiResponse.ok(wordService.wordOfTheDay());
    }

    @GetMapping("/most-viewed")
    public ApiResponse<List<WordResponse>> mostViewed(@RequestParam(defaultValue = "5") int limit) {
        return ApiResponse.ok(wordService.mostViewed(limit));
    }

    @GetMapping("/topics")
    public ApiResponse<List<String>> topics() {
        return ApiResponse.ok(wordService.topics());
    }

    @GetMapping("/topic/{topic}")
    public ApiResponse<PageResponse<WordResponse>> byTopic(@PathVariable String topic,
                                                            @RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "30") int size) {
        return ApiResponse.ok(PageResponse.from(wordService.searchByTopic(topic, page, size)));
    }

    @GetMapping("/stats")
    public ApiResponse<Map<String, Long>> stats() {
        return ApiResponse.ok(Map.of("totalWords", wordService.countWords()));
    }
}
