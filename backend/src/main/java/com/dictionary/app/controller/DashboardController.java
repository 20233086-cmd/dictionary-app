package com.dictionary.app.controller;

import com.dictionary.app.dto.response.ApiResponse;
import com.dictionary.app.dto.response.DashboardStatsResponse;
import com.dictionary.app.service.DashboardService;
import com.dictionary.app.repository.FavoriteRepository;
import com.dictionary.app.repository.FlashcardProgressRepository;
import com.dictionary.app.repository.SearchHistoryRepository;
import com.dictionary.app.security.CurrentUserProvider;
import com.dictionary.app.service.WordService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final CurrentUserProvider currentUserProvider;
    private final FavoriteRepository favoriteRepository;
    private final FlashcardProgressRepository flashcardRepository;
    private final SearchHistoryRepository historyRepository;
    private final WordService wordService;
    private final DashboardService dashboardService;

    public DashboardController(CurrentUserProvider currentUserProvider, FavoriteRepository favoriteRepository,
                               FlashcardProgressRepository flashcardRepository, SearchHistoryRepository historyRepository,
                               WordService wordService,
                               DashboardService dashboardService) {
        this.currentUserProvider = currentUserProvider;
        this.favoriteRepository = favoriteRepository;
        this.flashcardRepository = flashcardRepository;
        this.historyRepository = historyRepository;
        this.wordService = wordService;
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> dashboard() {
        var user = currentUserProvider.getCurrentUser();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalWords", wordService.countWords());
        data.put("favorites", favoriteRepository.findByUserOrderByCreatedAtDesc(user).size());
        data.put("flashcards", flashcardRepository.countByUser(user));
        data.put("dueToday", flashcardRepository.countDueToday(user, LocalDate.now()));
        data.put("history", historyRepository.countByUser(user));
        data.put("wordOfDay", wordService.wordOfTheDay());
        return ApiResponse.ok(data);
    }
    
    @GetMapping("/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getAdminStats() {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.getAdminStats()));
    }  
}
