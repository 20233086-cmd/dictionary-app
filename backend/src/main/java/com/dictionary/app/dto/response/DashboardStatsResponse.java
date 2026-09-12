package com.dictionary.app.dto.response;

public class DashboardStatsResponse {

    private long totalWords;
    private long totalUsers;
    private long totalSearches;
    private long totalFavorites;
    private long totalFlashcards;

    public DashboardStatsResponse() {
    }

    public DashboardStatsResponse(long totalWords, long totalUsers, long totalSearches, long totalFavorites, long totalFlashcards) {
        this.totalWords = totalWords;
        this.totalUsers = totalUsers;
        this.totalSearches = totalSearches;
        this.totalFavorites = totalFavorites;
        this.totalFlashcards = totalFlashcards;
    }

    // Getters and Setters
    public long getTotalWords() {
        return totalWords;
    }

    public void setTotalWords(long totalWords) {
        this.totalWords = totalWords;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalSearches() {
        return totalSearches;
    }

    public void setTotalSearches(long totalSearches) {
        this.totalSearches = totalSearches;
    }

    public long getTotalFavorites() {
        return totalFavorites;
    }

    public void setTotalFavorites(long totalFavorites) {
        this.totalFavorites = totalFavorites;
    }

    public long getTotalFlashcards() {
        return totalFlashcards;
    }

    public void setTotalFlashcards(long totalFlashcards) {
        this.totalFlashcards = totalFlashcards;
    }
}
