package com.nhnacademy.ailibraryteam1.telegram.dto;

import com.nhnacademy.ailibraryteam1.book.dto.BookAiRecommendationResponse;
import com.nhnacademy.ailibraryteam1.book.entity.Book;

public record TelegramBookSearchResult(
        long id,
        String title,
        String authorName,
        String publisherName,
        String imageUrl,
        int relevance,
        String reason,
        Double preferenceScore
) {
    public static TelegramBookSearchResult of(Book book, BookAiRecommendationResponse aiResponse, Double preferenceScore) {
        return new TelegramBookSearchResult(
                book.getId(),
                book.getTitle(),
                book.getAuthorName(),
                book.getPublisherName(),
                book.getImageUrl(),
                aiResponse.relevance(),
                aiResponse.why(),
                preferenceScore
        );
    }

    public String getPreferencePercent() {
        if (preferenceScore == null || preferenceScore <= 0.0) {
            return "정보 없음 (평가 부족)";
        }

        return String.format("%.1f%%", preferenceScore * 100);
    }
}
