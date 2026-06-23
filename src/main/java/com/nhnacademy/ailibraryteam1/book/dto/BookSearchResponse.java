package com.nhnacademy.ailibraryteam1.book.dto;

import com.querydsl.core.annotations.QueryProjection;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BookSearchResponse(
        long id,
        String isbn,
        String title,
        String volumeTitle,
        String authorName,
        String publisherName,
        BigDecimal price,
        LocalDate editionPublishDate,
        String bookContent,
        String imageUrl,
        Double similarity
) {
    // 타입 안정성 위해
    // 빌드 시 QBookSearchResponse 생성됨
    @QueryProjection
    public BookSearchResponse(
            long id,
            String isbn,
            String title,
            String volumeTitle,
            String authorName,
            String publisherName,
            BigDecimal price,
            LocalDate editionPublishDate,
            String bookContent,
            String imageUrl,
            Double similarity
    ) {
        this.id = id;
        this.isbn = isbn;
        this.title = title;
        this.volumeTitle = volumeTitle;
        this.authorName = authorName;
        this.publisherName = publisherName;
        this.price = price;
        this.editionPublishDate = editionPublishDate;
        this.bookContent = bookContent;
        this.imageUrl = imageUrl;
        this.similarity = similarity;
    }
}