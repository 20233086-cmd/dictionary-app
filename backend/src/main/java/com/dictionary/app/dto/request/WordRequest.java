package com.dictionary.app.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public class WordRequest {
    @NotBlank(message = "Từ tiếng Anh không được để trống")
    private String english;

    private String phonetic;

    private String level;
    private String topic;

    @Valid
    private List<MeaningRequest> meanings;

    public String getEnglish() { return english; }
    public void setEnglish(String english) { this.english = english; }

    public String getPhonetic() { return phonetic; }
    public void setPhonetic(String phonetic) { this.phonetic = phonetic; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public List<MeaningRequest> getMeanings() { return meanings; }
    public void setMeanings(List<MeaningRequest> meanings) { this.meanings = meanings; }
}
