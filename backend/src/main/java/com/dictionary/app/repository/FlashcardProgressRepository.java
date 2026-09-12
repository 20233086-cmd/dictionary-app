package com.dictionary.app.repository;

import com.dictionary.app.entity.FlashcardProgress;
import com.dictionary.app.entity.User;
import com.dictionary.app.entity.Word;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FlashcardProgressRepository extends JpaRepository<FlashcardProgress, Long> {

    Optional<FlashcardProgress> findByUserAndWord(User user, Word word);

    List<FlashcardProgress> findByUserOrderByNextReviewDateAsc(User user);

    @Query("SELECT f FROM FlashcardProgress f WHERE f.user = :user AND f.nextReviewDate <= :today ORDER BY f.nextReviewDate ASC")
    List<FlashcardProgress> findDue(@Param("user") User user, @Param("today") LocalDate today, Pageable pageable);

    long countByUserAndNextReviewDateLessThanEqual(User user, LocalDate today);

    default long countDueToday(User user, LocalDate today) { return countByUserAndNextReviewDateLessThanEqual(user, today); }

    long countByUser(User user);

    void deleteByUserAndWord(User user, Word word);

    boolean existsByUserAndWord(User user, Word word);
}
