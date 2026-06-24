package com.nhnacademy.ailibraryteam1.feedback.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FeedbackType {
    GOOD(1),
    BAD(-1);

    private final int score;
}
