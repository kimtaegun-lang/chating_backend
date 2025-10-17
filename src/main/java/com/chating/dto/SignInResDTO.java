package com.chating.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SignInResDTO {
	private Map<String,String> tokens; // access 토큰 및 refresh 토큰
	private String message; // 메세지
}
