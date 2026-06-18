package com.nhnacademy.ailibraryteam1.book.dto;

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
        String imageUrl
) {}
