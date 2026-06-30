package com.nhnacademy.ailibraryteam1.telegram.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "telegram.bot")
public record TelegramBotProperties(
    boolean enabled,
    String username,
    String token
) {}
