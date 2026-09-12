package com.dictionary.app.dto.response;

import java.util.List;

public class QuizQuestionResponse {
    private Long wordId;
    private String english;
    private String phonetic;
    private List<String> options;
    private int correctIndex;

    public QuizQuestionResponse() {
    }

    public QuizQuestionResponse(Long wordId, String english, String phonetic, List<String> options, int correctIndex) {
        this.wordId = wordId;
        this.english = english;
        this.phonetic = phonetic;
        this.options = options;
        this.correctIndex = correctIndex;
    }

    public Long getWordId() { return wordId; }
    public void setWordId(Long wordId) { this.wordId = wordId; }

    public String getEnglish() { return english; }
    public void setEnglish(String english) { this.english = english; }

    public String getPhonetic() { return phonetic; }
    public void setPhonetic(String phonetic) { this.phonetic = phonetic; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }

    public int getCorrectIndex() { return correctIndex; }
    public void setCorrectIndex(int correctIndex) { this.correctIndex = correctIndex; }
}
