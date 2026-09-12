package com.dictionary.app.controller;

import com.dictionary.app.dto.request.WordRequest;
import com.dictionary.app.dto.response.ApiResponse;
import com.dictionary.app.dto.response.PageResponse;
import com.dictionary.app.dto.response.WordResponse;
import com.dictionary.app.service.WordService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/words")
public class AdminWordController {

    private final WordService wordService;

    public AdminWordController(WordService wordService) {
        this.wordService = wordService;
    }

    @GetMapping
    public ApiResponse<PageResponse<WordResponse>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(PageResponse.from(wordService.search(keyword, page, size)));
    }

    @PostMapping
    public ApiResponse<WordResponse> create(@Valid @RequestBody WordRequest request) {
        return ApiResponse.ok("Đã thêm từ vựng mới", wordService.createWord(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<WordResponse> update(@PathVariable Long id, @Valid @RequestBody WordRequest request) {
        return ApiResponse.ok("Đã cập nhật từ vựng", wordService.updateWord(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        wordService.deleteWord(id);
        return ApiResponse.ok("Đã xoá từ vựng", null);
    }
}
