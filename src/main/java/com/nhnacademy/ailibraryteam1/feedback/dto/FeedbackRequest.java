package com.nhnacademy.ailibraryteam1.feedback.dto;

import com.nhnacademy.ailibraryteam1.feedback.entity.FeedbackType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FeedbackRequest(
        Long bookId,

        @NotBlank
        String query,

        @NotNull
        FeedbackType type
) {}
