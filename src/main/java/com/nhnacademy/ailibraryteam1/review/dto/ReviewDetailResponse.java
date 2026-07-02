package com.nhnacademy.ailibraryteam1.review.dto;

import java.math.BigDecimal;
import java.util.List;

public record ReviewDetailResponse(
        BigDecimal averageRating,
        long reviewCount,
        int rating1Count,
        int rating2Count,
        int rating3Count,
        int rating4Count,
        int rating5Count,
        List<ReviewResponse> reviews
) {
}