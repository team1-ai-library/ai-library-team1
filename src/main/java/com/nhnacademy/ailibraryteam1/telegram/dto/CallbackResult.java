package com.nhnacademy.ailibraryteam1.telegram.dto;

public record CallbackResult(
        String callbackQueryId,
        boolean success,
        String message,
        boolean isAlert
) {}
