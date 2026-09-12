package com.dictionary.app.dto.response;

import com.dictionary.app.entity.Word;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class WordResponse {
    private Long id;
    private String english;
    private String phonetic;
    private String level;
    private String topic;
    private Long viewCount;
    private List<MeaningResponse> meanings;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean favorited;

    public WordResponse() {
    }

    public WordResponse(Long id, String english, String phonetic, String level, String topic, Long viewCount,
                         List<MeaningResponse> meanings, LocalDateTime createdAt, LocalDateTime updatedAt,
                         boolean favorited) {
        this.id = id;
        this.english = english;
        this.phonetic = phonetic;
        this.level = level;
        this.topic = topic;
        this.viewCount = viewCount;
        this.meanings = meanings;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.favorited = favorited;
    }

    public static WordResponse fromEntity(Word w) {
        List<MeaningResponse> meaningResponses = w.getMeanings() == null ? List.of() :
                w.getMeanings().stream()
                        .sorted((a, b) -> Integer.compare(
                                a.getOrderIndex() == null ? 0 : a.getOrderIndex(),
                                b.getOrderIndex() == null ? 0 : b.getOrderIndex()))
                        .map(MeaningResponse::fromEntity)
                        .collect(Collectors.toList());

        return new WordResponse(
                w.getId(),
                w.getEnglish(),
                w.getPhonetic(),
                w.getLevel(),
                w.getTopic(),
                w.getViewCount(),
                meaningResponses,
                w.getCreatedAt(),
                w.getUpdatedAt(),
                false
        );
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEnglish() { return english; }
    public void setEnglish(String english) { this.english = english; }

    public String getPhonetic() { return phonetic; }
    public void setPhonetic(String phonetic) { this.phonetic = phonetic; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public Long getViewCount() { return viewCount; }
    public void setViewCount(Long viewCount) { this.viewCount = viewCount; }

    public List<MeaningResponse> getMeanings() { return meanings; }
    public void setMeanings(List<MeaningResponse> meanings) { this.meanings = meanings; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public boolean isFavorited() { return favorited; }
    public void setFavorited(boolean favorited) { this.favorited = favorited; }
}
