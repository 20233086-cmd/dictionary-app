package com.dictionary.app.service;

import com.dictionary.app.dto.response.WordResponse;
import com.dictionary.app.entity.Favorite;
import com.dictionary.app.entity.User;
import com.dictionary.app.entity.Word;
import com.dictionary.app.exception.BadRequestException;
import com.dictionary.app.exception.ResourceNotFoundException;
import com.dictionary.app.repository.FavoriteRepository;
import com.dictionary.app.repository.WordRepository;
import com.dictionary.app.security.CurrentUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final WordRepository wordRepository;
    private final CurrentUserProvider currentUserProvider;

    public FavoriteService(FavoriteRepository favoriteRepository, WordRepository wordRepository,
                            CurrentUserProvider currentUserProvider) {
        this.favoriteRepository = favoriteRepository;
        this.wordRepository = wordRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional
    public void addFavorite(Long wordId) {
        User user = currentUserProvider.getCurrentUser();
        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy từ vựng"));

        if (favoriteRepository.existsByUserAndWord(user, word)) {
            throw new BadRequestException("Từ này đã có trong danh sách yêu thích");
        }

        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setWord(word);
        favoriteRepository.save(favorite);
    }

    @Transactional
    public void removeFavorite(Long wordId) {
        User user = currentUserProvider.getCurrentUser();
        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy từ vựng"));
        favoriteRepository.deleteByUserAndWord(user, word);
    }

    @Transactional(readOnly = true)
    public List<WordResponse> getMyFavorites() {
        User user = currentUserProvider.getCurrentUser();
        return favoriteRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(f -> {
                    WordResponse dto = WordResponse.fromEntity(f.getWord());
                    dto.setFavorited(true);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean isFavorited(Long wordId) {
        User user = currentUserProvider.getCurrentUser();
        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy từ vựng"));
        return favoriteRepository.existsByUserAndWord(user, word);
    }
}
