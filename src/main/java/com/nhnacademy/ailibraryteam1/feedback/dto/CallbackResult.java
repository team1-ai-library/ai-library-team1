package com.nhnacademy.ailibraryteam1.feedback.dto;

public record CallbackResult(
        String callbackQueryId,
        boolean success,
        String message,
        boolean isAlert
) {}
