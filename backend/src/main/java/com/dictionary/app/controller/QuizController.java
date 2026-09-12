package com.dictionary.app.controller;

import com.dictionary.app.dto.response.ApiResponse;
import com.dictionary.app.dto.response.QuizQuestionResponse;
import com.dictionary.app.service.QuizService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/quiz")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping("/daily")
    public ApiResponse<List<QuizQuestionResponse>> daily(@RequestParam(defaultValue = "10") int count) {
        return ApiResponse.ok(quizService.generateDaily(count));
    }
}
