package com.nhnacademy.ailibraryteam1.book.search;

import com.nhnacademy.ailibraryteam1.common.exception.BusinessException;
import com.nhnacademy.ailibraryteam1.common.exception.ErrorCode;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum SearchType {
    KEYWORD,
    VECTOR,
    HYBRID;

    public static SearchType from(String value) {
        return Arrays.stream(SearchType.values())
                .filter(searchType -> searchType.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.UNSUPPORTED_SEARCH_TYPE));
    }
}