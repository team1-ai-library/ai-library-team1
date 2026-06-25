package com.nhnacademy.ailibraryteam1.feedback.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FeedbackType {
    GOOD(1.0),
    BAD(-1.0);

    private final double score;
}
