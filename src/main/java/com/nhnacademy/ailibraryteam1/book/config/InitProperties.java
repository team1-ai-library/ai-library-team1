package com.nhnacademy.ailibraryteam1.book.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 야멜에서 설정 읽어오기
 */
@Component
@Getter
@Setter
@ToString
@ConfigurationProperties(prefix = "init")
public class InitProperties {

    private String bookFile;
    private boolean enable;
}