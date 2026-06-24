package com.nhnacademy.ailibraryteam1.feedback.dto;

import com.nhnacademy.ailibraryteam1.feedback.entity.Feedback;
import com.nhnacademy.ailibraryteam1.feedback.entity.FeedbackType;

import java.util.List;

public record FeedbackStatisticResponse(
    long goodCount,
    long badCount,
    long totalCount,
    double goodRatio,
    double feedbackScore
) {
    public static FeedbackStatisticResponse from(List<Feedback> feedbacks) {
        if (feedbacks == null || feedbacks.isEmpty()) {
            return new FeedbackStatisticResponse(0L, 0L, 0L, 0, 0);
        }

        long goodCount = feedbacks.stream()
                .filter(f -> f.getType() == FeedbackType.GOOD)
                .count();
        long badCount = feedbacks.stream()
                .filter(f -> f.getType() == FeedbackType.BAD)
                .count();

        long totalCount = feedbacks.size();

        double goodRatio = (double) goodCount / totalCount;
        double feedbackScore = (double) (goodCount - badCount) / totalCount;

        return new FeedbackStatisticResponse(goodCount, badCount, totalCount, goodRatio, feedbackScore);
    }
}
