package com.chating.dto;

import org.springframework.http.ResponseEntity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SignInResDTO {
	private String token; // jwt 토큰
	private String message; // 메세지
}
