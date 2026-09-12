package com.dictionary.app.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CorrectionRequest {
    @NotBlank(message = "Vui lòng nhập câu cần kiểm tra")
    @Size(max = 1000, message = "Câu không được dài quá 1000 ký tự")
    private String sentence;
    public String getSentence() { return sentence; }
    public void setSentence(String sentence) { this.sentence = sentence; }
}
