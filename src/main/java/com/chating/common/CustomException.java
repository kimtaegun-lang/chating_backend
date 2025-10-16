package com.chating.common;

import org.springframework.http.HttpStatus;

public class CustomException extends RuntimeException {

	private final HttpStatus status; // HTTP 상태 코드

 // 에러 코드와 메시지 포함
 public CustomException(HttpStatus status, String message) {
     super(message);
     this.status=status;
 }

 public HttpStatus getStatus() {
     return status;
 }
}
