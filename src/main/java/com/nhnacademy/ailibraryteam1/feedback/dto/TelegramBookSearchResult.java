package com.nhnacademy.ailibraryteam1.feedback.dto;

import com.nhnacademy.ailibraryteam1.book.dto.BookAiRecommendationResponse;
import com.nhnacademy.ailibraryteam1.book.entity.Book;

public record TelegramBookSearchResult(
        long id,
        String title,
        String authorName,
        String publisherName,
        String imageUrl,
        int relevance,
        String reason
) {
    public static TelegramBookSearchResult of(Book book, BookAiRecommendationResponse aiResponse) {
        return new TelegramBookSearchResult(
                book.getId(),
                book.getTitle(),
                book.getAuthorName(),
                book.getPublisherName(),
                book.getImageUrl(),
                aiResponse.relevance(),
                aiResponse.why()
        );
    }
}
