package com.dictionary.app.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "meanings")
public class Meaning {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id", nullable = false)
    @JsonIgnore
    private Word word;

    @Column(length = 30)
    private String partOfSpeech; // noun, verb, adjective...

    @Column(nullable = false, length = 500)
    private String vietnamese; // nghĩa tiếng Việt

    @Column(length = 500)
    private String exampleEn;

    @Column(length = 500)
    private String exampleVi;

    @Column(length = 300)
    private String synonyms; // phân tách bởi dấu phẩy

    @Column(name = "order_index")
    private Integer orderIndex = 0;

    public Meaning() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Word getWord() { return word; }
    public void setWord(Word word) { this.word = word; }

    public String getPartOfSpeech() { return partOfSpeech; }
    public void setPartOfSpeech(String partOfSpeech) { this.partOfSpeech = partOfSpeech; }

    public String getVietnamese() { return vietnamese; }
    public void setVietnamese(String vietnamese) { this.vietnamese = vietnamese; }

    public String getExampleEn() { return exampleEn; }
    public void setExampleEn(String exampleEn) { this.exampleEn = exampleEn; }

    public String getExampleVi() { return exampleVi; }
    public void setExampleVi(String exampleVi) { this.exampleVi = exampleVi; }

    public String getSynonyms() { return synonyms; }
    public void setSynonyms(String synonyms) { this.synonyms = synonyms; }

    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }
}
