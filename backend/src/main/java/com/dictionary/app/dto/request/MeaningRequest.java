package com.dictionary.app.dto.request;

public class MeaningRequest {
    private Long id; // null nếu tạo mới
    private String partOfSpeech;
    private String vietnamese;
    private String exampleEn;
    private String exampleVi;
    private String synonyms;
    private Integer orderIndex;

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
