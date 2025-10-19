package com.chating.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// 커스텀 및 다른 예외처리
@RestControllerAdvice
public class GlobalExceptionHandler {
	// 커스텀 예외 처리
    @ExceptionHandler(CustomException.class)
    public ResponseEntity handleCustomException(CustomException ex) {
        return new ResponseEntity(ex.getMessage(),ex.getStatus());
    }
    
    // 기타 모든 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity handleException(Exception ex) {
        ex.printStackTrace();
        return new ResponseEntity("서버 오류.",HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

