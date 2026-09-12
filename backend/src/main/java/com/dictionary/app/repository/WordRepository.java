package com.dictionary.app.repository;

import com.dictionary.app.entity.Word;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WordRepository extends JpaRepository<Word, Long> {

    Optional<Word> findByEnglishIgnoreCase(String english);

    Page<Word> findByEnglishContainingIgnoreCase(String keyword, Pageable pageable);

    @Query("SELECT w FROM Word w WHERE LOWER(w.english) LIKE LOWER(CONCAT(:prefix, '%')) ORDER BY w.english ASC")
    List<Word> findSuggestions(@Param("prefix") String prefix, Pageable pageable);

    @Query(value = "SELECT * FROM words ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Word findRandomWord();

    @Query("SELECT w FROM Word w ORDER BY w.viewCount DESC")
    List<Word> findTopViewed(Pageable pageable);

    @Query(value = "SELECT DISTINCT w FROM Word w JOIN w.meanings m WHERE LOWER(m.vietnamese) LIKE LOWER(CONCAT('%', :keyword, '%'))",
           countQuery = "SELECT COUNT(DISTINCT w) FROM Word w JOIN w.meanings m WHERE LOWER(m.vietnamese) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Word> searchByVietnameseMeaning(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT w.english FROM Word w")
    List<String> findAllEnglishWords();

    @Query("SELECT DISTINCT w.topic FROM Word w WHERE w.topic IS NOT NULL AND w.topic <> '' ORDER BY w.topic")
    List<String> findDistinctTopics();

    Page<Word> findByTopicIgnoreCase(String topic, Pageable pageable);
}
