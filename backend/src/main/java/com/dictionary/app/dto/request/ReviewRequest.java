package com.dictionary.app.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ReviewRequest {
    /**
     * Chất lượng ghi nhớ theo thang SM-2 (0-5).
     * Frontend chỉ dùng 3 mức: 1 = Chưa nhớ, 3 = Nhớ, 5 = Dễ.
     */
    @NotNull
    @Min(0)
    @Max(5)
    private Integer quality;

    public Integer getQuality() { return quality; }
    public void setQuality(Integer quality) { this.quality = quality; }
}
