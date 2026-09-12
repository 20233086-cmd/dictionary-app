package com.dictionary.app.service;

import com.dictionary.app.entity.SearchHistory;
import com.dictionary.app.entity.User;
import com.dictionary.app.repository.SearchHistoryRepository;
import com.dictionary.app.security.CurrentUserProvider;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchHistoryService {

    private final SearchHistoryRepository searchHistoryRepository;
    private final CurrentUserProvider currentUserProvider;

    public SearchHistoryService(SearchHistoryRepository searchHistoryRepository, CurrentUserProvider currentUserProvider) {
        this.searchHistoryRepository = searchHistoryRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional
    public void record(String keyword) {
        try {
            User user = currentUserProvider.getCurrentUser();
            SearchHistory history = new SearchHistory();
            history.setUser(user);
            history.setKeyword(keyword);
            searchHistoryRepository.save(history);
        } catch (Exception ignored) {
            // Khách vãng lai (chưa đăng nhập) tra từ -> không lưu lịch sử, bỏ qua lỗi
        }
    }

    @Transactional(readOnly = true)
    public List<String> getMyHistory(int limit) {
        User user = currentUserProvider.getCurrentUser();
        return searchHistoryRepository.findByUserOrderBySearchedAtDesc(user, PageRequest.of(0, limit))
                .stream()
                .map(SearchHistory::getKeyword)
                .collect(Collectors.toList());
    }

    @Transactional
    public void clearMyHistory() {
        User user = currentUserProvider.getCurrentUser();
        searchHistoryRepository.deleteByUser(user);
    }
}
