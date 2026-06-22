package com.nhnacademy.ailibraryteam1.review.dto;

import com.nhnacademy.ailibraryteam1.book.entity.Book;
import com.nhnacademy.ailibraryteam1.review.entity.BookReview;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReviewCreateRequest(
        @NotBlank
        String content,

        @NotNull
        @Min(1)
        @Max(5)
        int rating
) {
        public BookReview toEntity(Book book) {
                return BookReview.create(book, content, rating);
        }
}
