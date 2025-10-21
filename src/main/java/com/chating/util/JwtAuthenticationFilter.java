package com.chating.util;

import java.io.IOException;
import java.util.ArrayList;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.chating.common.CustomException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            // 토큰이 유효 할 경우엔
            if (jwtUtil.isTokenValid(token)) {
            	// 토큰에서 회원 이름 추출
                String username = jwtUtil.extractUsername(token);
                // SecurityContext에 인증 등록
                UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>());
                												// 사용자, 비밀번호, 권한
                    SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            else {
                // 토큰 만료 시 401 응답 직접 반환
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\":\"UNAUTHORIZED\",\"message\":\"토큰이 만료되었습니다.\"}");
                return; // 여기서 필터 체인 중단! (중요)
            }
        }
        // 다음 필터 실행
        filterChain.doFilter(request, response);
    }
}