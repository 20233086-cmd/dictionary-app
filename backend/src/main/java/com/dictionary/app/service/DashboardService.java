package com.dictionary.app.service;

import com.dictionary.app.dto.response.DashboardStatsResponse;
import com.dictionary.app.repository.FavoriteRepository;
import com.dictionary.app.repository.FlashcardProgressRepository;
import com.dictionary.app.repository.SearchHistoryRepository;
import com.dictionary.app.repository.UserRepository;
import com.dictionary.app.repository.WordRepository;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private final WordRepository wordRepository;
    private final UserRepository userRepository;
    private final SearchHistoryRepository searchHistoryRepository;
    private final FavoriteRepository favoriteRepository;
    private final FlashcardProgressRepository flashcardProgressRepository;

    public DashboardService(WordRepository wordRepository,
                            UserRepository userRepository,
                            SearchHistoryRepository searchHistoryRepository,
                            FavoriteRepository favoriteRepository,
                            FlashcardProgressRepository flashcardProgressRepository) {
        this.wordRepository = wordRepository;
        this.userRepository = userRepository;
        this.searchHistoryRepository = searchHistoryRepository;
        this.favoriteRepository = favoriteRepository;
        this.flashcardProgressRepository = flashcardProgressRepository;
    }

    public DashboardStatsResponse getAdminStats() {
        long totalWords = wordRepository.count();
        long totalUsers = userRepository.count();
        long totalSearches = searchHistoryRepository.count();
        long totalFavorites = favoriteRepository.count();
        long totalFlashcards = flashcardProgressRepository.count();

        return new DashboardStatsResponse(
                totalWords,
                totalUsers,
                totalSearches,
                totalFavorites,
                totalFlashcards
        );
    }
}
