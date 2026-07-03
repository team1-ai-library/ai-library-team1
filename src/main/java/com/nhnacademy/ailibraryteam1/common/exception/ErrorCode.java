package com.nhnacademy.ailibraryteam1.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "유효하지 않은 입력값입니다."),
    BOOK_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 도서입니다."),
    RATING_INVALID(HttpStatus.BAD_REQUEST, "유효하지 않은 평점입니다."),
    STATISTIC_NOT_FOUND(HttpStatus.NOT_FOUND, "리뷰 통계 정보를 찾을 수 없습니다."),
    SUMMARY_NOT_FOUND(HttpStatus.NOT_FOUND, "AI 리뷰 요약을 찾을 수 없습니다."),
    UNSUPPORTED_SEARCH_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 검색 타입입니다."),
    UNSUPPORTED_MODEL(HttpStatus.BAD_REQUEST, "지원하지 않는 모델 타입입니다."),
    AI_MODEL_CALL_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "AI 모델 호출에 실패했습니다."), // AI 서버가 일시적으로 응답하지 못 하는 상황이라서 (서버 문제)
    SEARCH_CONDITION_REQUIRED(HttpStatus.BAD_REQUEST, "검색어 또는 ISBN을 입력해주세요."),
    HASH_ALGORITHM_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "해시 알고리즘을 찾을 수 없습니다."),
    FEEDBACK_DUPLICATED(HttpStatus.CONFLICT, "이미 피드백을 남긴 검색입니다."),
    CALLBACK_DATA_INVALID(HttpStatus.BAD_REQUEST, "유효하지 않은 콜백 데이터입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;
}