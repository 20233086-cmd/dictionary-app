package com.dictionary.app.controller;

import com.dictionary.app.dto.response.ApiResponse;
import com.dictionary.app.service.SearchHistoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/history")
public class HistoryController {

    private final SearchHistoryService searchHistoryService;

    public HistoryController(SearchHistoryService searchHistoryService) {
        this.searchHistoryService = searchHistoryService;
    }

    @GetMapping
    public ApiResponse<List<String>> myHistory(@RequestParam(defaultValue = "30") int limit) {
        return ApiResponse.ok(searchHistoryService.getMyHistory(limit));
    }

    @DeleteMapping
    public ApiResponse<Void> clear() {
        searchHistoryService.clearMyHistory();
        return ApiResponse.ok("Đã xoá lịch sử tra cứu", null);
    }
}
