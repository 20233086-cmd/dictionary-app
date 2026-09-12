package com.dictionary.app.dto.response;

import com.dictionary.app.entity.FlashcardProgress;

import java.time.LocalDate;

public class FlashcardResponse {
    private Long progressId;
    private WordResponse word;
    private Integer repetitions;
    private LocalDate nextReviewDate;

    public FlashcardResponse() {
    }

    public FlashcardResponse(Long progressId, WordResponse word, Integer repetitions, LocalDate nextReviewDate) {
        this.progressId = progressId;
        this.word = word;
        this.repetitions = repetitions;
        this.nextReviewDate = nextReviewDate;
    }

    public static FlashcardResponse fromEntity(FlashcardProgress p) {
        return new FlashcardResponse(
                p.getId(),
                WordResponse.fromEntity(p.getWord()),
                p.getRepetitions(),
                p.getNextReviewDate()
        );
    }

    public Long getProgressId() { return progressId; }
    public void setProgressId(Long progressId) { this.progressId = progressId; }

    public WordResponse getWord() { return word; }
    public void setWord(WordResponse word) { this.word = word; }

    public Integer getRepetitions() { return repetitions; }
    public void setRepetitions(Integer repetitions) { this.repetitions = repetitions; }

    public LocalDate getNextReviewDate() { return nextReviewDate; }
    public void setNextReviewDate(LocalDate nextReviewDate) { this.nextReviewDate = nextReviewDate; }
}
