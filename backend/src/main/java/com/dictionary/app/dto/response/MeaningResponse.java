package com.dictionary.app.dto.response;

import com.dictionary.app.entity.Meaning;

public class MeaningResponse {
    private Long id;
    private String partOfSpeech;
    private String vietnamese;
    private String exampleEn;
    private String exampleVi;
    private String synonyms;
    private Integer orderIndex;

    public MeaningResponse() {
    }

    public MeaningResponse(Long id, String partOfSpeech, String vietnamese, String exampleEn,
                            String exampleVi, String synonyms, Integer orderIndex) {
        this.id = id;
        this.partOfSpeech = partOfSpeech;
        this.vietnamese = vietnamese;
        this.exampleEn = exampleEn;
        this.exampleVi = exampleVi;
        this.synonyms = synonyms;
        this.orderIndex = orderIndex;
    }

    public static MeaningResponse fromEntity(Meaning m) {
        return new MeaningResponse(
                m.getId(),
                m.getPartOfSpeech(),
                m.getVietnamese(),
                m.getExampleEn(),
                m.getExampleVi(),
                m.getSynonyms(),
                m.getOrderIndex()
        );
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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
