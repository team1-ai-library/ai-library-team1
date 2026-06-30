package com.nhnacademy.ailibraryteam1.rabbitmq.event;

import com.nhnacademy.ailibraryteam1.book.entity.Book;

public record ReviewEmbeddingEvent(
        BookDto bookDto,
        String reviewSummary
) {
    public static ReviewEmbeddingEvent create(Book book, String reviewSummary) {
        return new ReviewEmbeddingEvent(
                BookDto.create(book),
                reviewSummary
        );
    }
}
