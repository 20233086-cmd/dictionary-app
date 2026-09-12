package com.dictionary.app.repository;

import com.dictionary.app.entity.Favorite;
import com.dictionary.app.entity.User;
import com.dictionary.app.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findByUserOrderByCreatedAtDesc(User user);
    Optional<Favorite> findByUserAndWord(User user, Word word);
    boolean existsByUserAndWord(User user, Word word);
    void deleteByUserAndWord(User user, Word word);
}
