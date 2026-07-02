package com.nhnacademy.ailibraryteam1.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 정적 리소스 누락(favicon.ico)은 무해하므로, 로그 없이 조용히 404만 리턴
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Void> handleNoResourceFound(NoResourceFoundException e) {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        ErrorResponse response = ErrorResponse.from(e.getErrorCode());

        log.warn("비즈니스 로직 오류 발생: {}", e.getDetailMessage());

        return ResponseEntity.status(response.status()).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        ErrorResponse response = ErrorResponse.from(ErrorCode.INTERNAL_SERVER_ERROR);

        log.error("서버 오류 발생: {}", e.getMessage(), e);

        return ResponseEntity.status(response.status()).body(response);
    }
}