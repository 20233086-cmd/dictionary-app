package com.dictionary.app.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "words", indexes = {
        @Index(name = "idx_word_english", columnList = "english")
})
public class Word {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String english;

    @Column(length = 100)
    private String phonetic; // ví dụ: /həˈloʊ/

    @Column(length = 5)
    private String level; // A1, A2, B1, B2, C1, C2

    @Column(length = 80)
    private String topic; // Technology, Education, Travel...

    @Column(name = "view_count")
    private Long viewCount = 0L;

    @OneToMany(mappedBy = "word", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Meaning> meanings = new ArrayList<>();

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Word() {
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
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

    public List<Meaning> getMeanings() { return meanings; }
    public void setMeanings(List<Meaning> meanings) { this.meanings = meanings; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
