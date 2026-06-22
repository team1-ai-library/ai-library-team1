package com.nhnacademy.ailibraryteam1.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    BOOK_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 도서입니다."),
    RATING_INVALID(HttpStatus.BAD_REQUEST, "유효하지 않은 평점입니다."),
    STATISTIC_NOT_FOUND(HttpStatus.NOT_FOUND, "리뷰 통계 정보를 찾을 수 없습니다."),
    UNSUPPORTED_SEARCH_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 검색 타입입니다."),
    SEARCH_CONDITION_REQUIRED(HttpStatus.BAD_REQUEST, "검색어 또는 ISBN을 입력해주세요."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;
}