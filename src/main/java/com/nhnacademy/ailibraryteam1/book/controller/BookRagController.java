package com.nhnacademy.ailibraryteam1.book.controller;

import com.nhnacademy.ailibraryteam1.book.dto.BookAiRecommendationResponse;
import com.nhnacademy.ailibraryteam1.book.service.BookRagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BookRagController {

    private final BookRagService bookRagService;

    /**
     * RAG 추천은 페이징 안 함.
     * 일반 검색은 15만 건 중 결과를 가져오니 페이징 필수지만,
     * RAG 추천은 상위 10권만 뽑아서 AI에게 넘기고, AI가 그 중에서 관련성 높은 최대 5권을 선별해서 반환하므로.
     */
    @GetMapping("/books/recommend/{model}")
    public ResponseEntity<List<BookAiRecommendationResponse>> recommend(
            @PathVariable String model,
            @RequestParam String question) {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.bookRagService.recommendBooks(question, model));
    }
}