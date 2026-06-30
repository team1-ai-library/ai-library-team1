package com.nhnacademy.ailibraryteam1.review.dto;

import java.time.OffsetDateTime;

public record ReviewResponse(
        Long id,
        String content,
        int rating,
        OffsetDateTime createdAt
) {}