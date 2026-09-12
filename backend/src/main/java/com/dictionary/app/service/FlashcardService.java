package com.dictionary.app.service;

import com.dictionary.app.dto.response.FlashcardResponse;
import com.dictionary.app.entity.FlashcardProgress;
import com.dictionary.app.entity.User;
import com.dictionary.app.entity.Word;
import com.dictionary.app.exception.BadRequestException;
import com.dictionary.app.exception.ResourceNotFoundException;
import com.dictionary.app.repository.FlashcardProgressRepository;
import com.dictionary.app.repository.WordRepository;
import com.dictionary.app.security.CurrentUserProvider;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Quản lý bộ thẻ ôn tập (flashcard) của người dùng theo thuật toán lặp lại ngắt quãng SM-2
 * (nguyên lý tương tự Anki): mỗi lần ôn, người dùng tự đánh giá mức độ nhớ, hệ thống tự
 * tính ngày ôn tập tiếp theo — nhớ tốt thì giãn cách xa hơn, nhớ kém thì hỏi lại sớm hơn.
 */
@Service
public class FlashcardService {

    private final FlashcardProgressRepository progressRepository;
    private final WordRepository wordRepository;
    private final CurrentUserProvider currentUserProvider;

    private static final double MIN_EASE_FACTOR = 1.3;

    public FlashcardService(FlashcardProgressRepository progressRepository, WordRepository wordRepository,
                             CurrentUserProvider currentUserProvider) {
        this.progressRepository = progressRepository;
        this.wordRepository = wordRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional
    public void addToDeck(Long wordId) {
        User user = currentUserProvider.getCurrentUser();
        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy từ vựng"));

        if (progressRepository.existsByUserAndWord(user, word)) {
            throw new BadRequestException("Từ này đã có trong bộ ôn tập");
        }

        FlashcardProgress progress = new FlashcardProgress();
        progress.setUser(user);
        progress.setWord(word);
        progress.setRepetitions(0);
        progress.setEaseFactor(2.5);
        progress.setIntervalDays(0);
        progress.setNextReviewDate(LocalDate.now());
        progressRepository.save(progress);
    }

    @Transactional
    public void removeFromDeck(Long wordId) {
        User user = currentUserProvider.getCurrentUser();
        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy từ vựng"));
        progressRepository.deleteByUserAndWord(user, word);
    }

    @Transactional(readOnly = true)
    public boolean isInDeck(Long wordId) {
        User user = currentUserProvider.getCurrentUser();
        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy từ vựng"));
        return progressRepository.existsByUserAndWord(user, word);
    }

    @Transactional(readOnly = true)
    public List<FlashcardResponse> getDueCards(int limit) {
        User user = currentUserProvider.getCurrentUser();
        return progressRepository.findDue(user, LocalDate.now(), PageRequest.of(0, limit))
                .stream().map(FlashcardResponse::fromEntity).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FlashcardResponse> getFullDeck() {
        User user = currentUserProvider.getCurrentUser();
        return progressRepository.findByUserOrderByNextReviewDateAsc(user)
                .stream().map(FlashcardResponse::fromEntity).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long countDueToday() {
        User user = currentUserProvider.getCurrentUser();
        return progressRepository.countByUserAndNextReviewDateLessThanEqual(user, LocalDate.now());
    }

    @Transactional(readOnly = true)
    public long countDeckSize() {
        User user = currentUserProvider.getCurrentUser();
        return progressRepository.countByUser(user);
    }

    /**
     * Ghi nhận kết quả ôn tập một thẻ theo thuật toán SM-2.
     * quality: thang 0-5 (frontend dùng 1 = Chưa nhớ, 3 = Nhớ, 5 = Dễ).
     */
    @Transactional
    public FlashcardResponse review(Long wordId, int quality) {
        User user = currentUserProvider.getCurrentUser();
        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy từ vựng"));

        FlashcardProgress progress = progressRepository.findByUserAndWord(user, word)
                .orElseThrow(() -> new ResourceNotFoundException("Từ này chưa có trong bộ ôn tập"));

        int repetitions = progress.getRepetitions() == null ? 0 : progress.getRepetitions();
        double easeFactor = progress.getEaseFactor() == null ? 2.5 : progress.getEaseFactor();
        int interval;

        if (quality < 3) {
            repetitions = 0;
            interval = 1;
        } else {
            if (repetitions == 0) {
                interval = 1;
            } else if (repetitions == 1) {
                interval = 6;
            } else {
                int previousInterval = progress.getIntervalDays() == null ? 1 : progress.getIntervalDays();
                interval = (int) Math.round(previousInterval * easeFactor);
            }
            repetitions++;
        }

        easeFactor = easeFactor + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02));
        if (easeFactor < MIN_EASE_FACTOR) easeFactor = MIN_EASE_FACTOR;

        progress.setRepetitions(repetitions);
        progress.setEaseFactor(easeFactor);
        progress.setIntervalDays(interval);
        progress.setNextReviewDate(LocalDate.now().plusDays(interval));
        progress.setLastReviewedAt(java.time.LocalDateTime.now());

        FlashcardProgress saved = progressRepository.save(progress);
        return FlashcardResponse.fromEntity(saved);
    }
}
