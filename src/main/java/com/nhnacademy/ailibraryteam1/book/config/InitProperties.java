package com.nhnacademy.ailibraryteam1.book.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 야멜에서 설정 읽어오기
 */
@ConfigurationProperties(prefix = "init")
public record InitProperties(
        String bookFile,
        boolean enable
) {
}