package com.nhnacademy.ailibraryteam1.common.util;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.util.StringUtils;

/**
 * 텍스트 전처리
 * AI 모델에 텍스트 보내기 전 정제
 */
public class TextPreProcessor {

    private static final String HTML_TAG_PATTERN = "<[^>]*>";
    private static final String SPECIAL_CHAR_PATTERN = "[^가-힣a-zA-Z0-9\\s]";
    private static final String SPACE_PATTERN = "\\s+";

    // 텍스트 전처리
    public static String preprocess(String text) {

        // 널 체크
        if (!StringUtils.hasText(text)) {
            return "";
        }

        return StringEscapeUtils
                .unescapeHtml4(text) // HTML 엔티티 디코딩
                .replaceAll(HTML_TAG_PATTERN, " ") // HTML 태그 제거
                .replaceAll(SPECIAL_CHAR_PATTERN, "") // 특수문자 제거
                .replaceAll(SPACE_PATTERN, " ") // 연속된 공백 통합
                .trim().toLowerCase(); // 앞뒤 공백 제거, 소문자 변환
    }
}