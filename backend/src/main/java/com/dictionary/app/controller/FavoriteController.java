package com.dictionary.app.controller;

import com.dictionary.app.dto.response.ApiResponse;
import com.dictionary.app.dto.response.WordResponse;
import com.dictionary.app.service.FavoriteService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping
    public ApiResponse<List<WordResponse>> myFavorites() {
        return ApiResponse.ok(favoriteService.getMyFavorites());
    }

    @PostMapping("/{wordId}")
    public ApiResponse<Void> add(@PathVariable Long wordId) {
        favoriteService.addFavorite(wordId);
        return ApiResponse.ok("Đã thêm vào yêu thích", null);
    }

    @DeleteMapping("/{wordId}")
    public ApiResponse<Void> remove(@PathVariable Long wordId) {
        favoriteService.removeFavorite(wordId);
        return ApiResponse.ok("Đã bỏ khỏi yêu thích", null);
    }

    @GetMapping("/{wordId}/check")
    public ApiResponse<Boolean> check(@PathVariable Long wordId) {
        return ApiResponse.ok(favoriteService.isFavorited(wordId));
    }
}
