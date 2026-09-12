package com.dictionary.app.controller;

import com.dictionary.app.dto.request.AiChatRequest;
import com.dictionary.app.dto.response.ApiResponse;
import com.dictionary.app.repository.ChatConversationRepository;
import com.dictionary.app.repository.FavoriteRepository;
import com.dictionary.app.repository.FlashcardProgressRepository;
import com.dictionary.app.repository.SearchHistoryRepository;
import com.dictionary.app.repository.UserRepository;
import com.dictionary.app.service.GeminiClient;
import com.dictionary.app.service.WordService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/ai")
public class AdminAiController {
    private final GeminiClient geminiClient;
    private final UserRepository userRepository;
    private final WordService wordService;
    private final SearchHistoryRepository historyRepository;
    private final FavoriteRepository favoriteRepository;
    private final FlashcardProgressRepository flashcardRepository;
    private final ChatConversationRepository conversationRepository;

    public AdminAiController(GeminiClient geminiClient, UserRepository userRepository,
                             WordService wordService, SearchHistoryRepository historyRepository,
                             FavoriteRepository favoriteRepository, FlashcardProgressRepository flashcardRepository,
                             ChatConversationRepository conversationRepository) {
        this.geminiClient = geminiClient;
        this.userRepository = userRepository;
        this.wordService = wordService;
        this.historyRepository = historyRepository;
        this.favoriteRepository = favoriteRepository;
        this.flashcardRepository = flashcardRepository;
        this.conversationRepository = conversationRepository;
    }

    @GetMapping("/overview")
    public ApiResponse<Map<String, Object>> overview() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalUsers", userRepository.count());
        data.put("totalWords", wordService.countWords());
        data.put("totalSearches", historyRepository.count());
        data.put("todaySearches", historyRepository.countBySearchedAtGreaterThanEqualAndSearchedAtLessThan(start, end));
        data.put("totalFavorites", favoriteRepository.count());
        data.put("totalFlashcards", flashcardRepository.count());
        data.put("totalConversations", conversationRepository.count());
        data.put("topWords", wordService.mostViewed(5));
        return ApiResponse.ok(data);
    }

    @PostMapping("/chat")
    public ApiResponse<String> chat(@Valid @RequestBody AiChatRequest request) {
        String context = buildContext();

        String prompt = """
                [DỮ LIỆU HỆ THỐNG THAM KHẢO]
                %s

                [YÊU CẦU CỦA QUẢN TRỊ VIÊN]
                %s

                LƯU Ý: Trả lời bằng Tiếng Việt. 
                - Nếu quản trị viên yêu cầu tạo/gợi ý nội dung cụ thể (từ vựng, ví dụ, quiz...), hãy trả lời TRỰC TIẾP nội dung đó. 
                - Chỉ đưa ra báo cáo thống kê/nhận xét khi quản trị viên yêu cầu phân tích hệ thống.
                """.formatted(context, request.getMessage().trim());

        return ApiResponse.ok(geminiClient.chat(
                List.of(GeminiClient.ChatTurn.user(prompt)), "ADMIN"));
    }

    private String buildContext() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();
        StringBuilder sb = new StringBuilder();
        sb.append("- Tổng người dùng: ").append(userRepository.count()).append('\n');
        sb.append("- Tổng từ vựng: ").append(wordService.countWords()).append('\n');
        sb.append("- Tổng lượt tra cứu: ").append(historyRepository.count()).append('\n');
        sb.append("- Lượt tra cứu hôm nay: ")
          .append(historyRepository.countBySearchedAtGreaterThanEqualAndSearchedAtLessThan(start, end)).append('\n');
        sb.append("- Tổng từ yêu thích: ").append(favoriteRepository.count()).append('\n');
        sb.append("- Tổng flashcard: ").append(flashcardRepository.count()).append('\n');
        sb.append("- Tổng cuộc hội thoại AI: ").append(conversationRepository.count()).append('\n');
        sb.append("- Top từ được tra nhiều: ");
        wordService.mostViewed(5).forEach(w -> sb.append(w.getEnglish()).append("; "));
        return sb.toString();
    }
}
