package com.nhnacademy.ailibraryteam1.book.dto;

/**
 * AI 추천 응답 DTO
 * AI가 생성한 추천 결과를 담음
 */
public record BookAiRecommendationResponse(
        long id, // 도서 ID
        int relevance, // 연관성 점수 (0 ~ 100)
        String why // 추천 사유
) {
}