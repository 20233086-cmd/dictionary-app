package com.dictionary.app.repository;

import com.dictionary.app.entity.SearchHistory;
import com.dictionary.app.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.time.LocalDateTime;

public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {
    List<SearchHistory> findByUserOrderBySearchedAtDesc(User user, Pageable pageable);
    void deleteByUser(User user);
    long countByUser(User user);

    long countBySearchedAtGreaterThanEqualAndSearchedAtLessThan(LocalDateTime start, LocalDateTime end);
}
