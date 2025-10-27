package com.chating.security;

import java.io.IOException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationErrorHandler implements AuthenticationEntryPoint {
    
	// 토큰 만료
	public void handleTokenExpired(HttpServletResponse response) throws IOException {
	    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
	    response.setContentType("application/json;charset=UTF-8");
	    response.getWriter().write("{\"error\":\"TOKEN_EXPIRED\",\"message\":\"토큰이 만료되었습니다.\"}");
	}

	// 유효하지 않은 토큰
	public void handleInvalidToken(HttpServletResponse response) throws IOException {
	    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
	    response.setContentType("application/json;charset=UTF-8");
	    response.getWriter().write("{\"error\":\"INVALID_TOKEN\",\"message\":\"유효하지 않은 토큰입니다.\"}");
	}

	// 인증 필요 (토큰 없음)
	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
	                    AuthenticationException authException) throws IOException {
	    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
	    response.setContentType("application/json;charset=UTF-8");
	    response.getWriter().write("{\"error\":\"NO_TOKEN\",\"message\":\"로그인이 필요합니다.\"}");
	}
	
	 
  
}