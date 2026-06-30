package com.nhnacademy.ailibraryteam1.feedback.dto;

import com.querydsl.core.annotations.QueryProjection;

public record BookFeedbackCount(
        long bookId,
        long goodCount,
        long totalCount
) {
    @QueryProjection
    public BookFeedbackCount(long bookId, long goodCount, long totalCount) {
        this.bookId = bookId;
        this.goodCount = goodCount;
        this.totalCount = totalCount;
    }
}
