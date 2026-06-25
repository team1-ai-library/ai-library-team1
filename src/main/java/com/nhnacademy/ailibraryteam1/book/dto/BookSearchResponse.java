package com.nhnacademy.ailibraryteam1.book.dto;

import com.querydsl.core.annotations.QueryProjection;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

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
        Double similarity, // 벡터 검색 유사도 (0 ~ 1)
        Double rrfScore // 하이브리드 검색 RRF 점
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
            Double similarity,
            Double rrfScore
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
        this.rrfScore = rrfScore;
    }

    // 유사도를 퍼센트로 변환
    public String getSimilarityPercent() {
        if(Objects.isNull(similarity)) {
            return null;
        }

        return String.format("%.1f%%", similarity * 100);
    }
}