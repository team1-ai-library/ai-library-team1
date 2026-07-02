package com.nhnacademy.ailibraryteam1.book.dto;

/**
 * AI 추천 결과(BookAiRecommendationResponse) + 도서 상세 정보(도서 제목, 저자, 출판사, 이미지) 결합 DTO
 */
public record BookRecommendationResult(
        long id,
        String title,
        String authorName,
        String publisherName,
        String imageUrl,
        int relevance,
        String why
) {
}