package com.chating.controller.member;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chating.common.CustomException;
import com.chating.entity.member.Role;
import com.chating.util.JwtUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class RefreshTokenController {
    
    private final JwtUtil jwtUtil;
    
    @PostMapping("/api/refresh")
    public ResponseEntity<String> refreshAccessToken(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {
        
        if (refreshToken == null || !jwtUtil.isTokenValid(refreshToken)) {
        	throw new CustomException(HttpStatus.FORBIDDEN, "세션이 만료되었습니다. 다시 로그인 해주세요.");
        }

        String newAccessToken = jwtUtil.generateAccessToken(jwtUtil.extractUsername(refreshToken), Role.USER);
        
        Cookie accessCookie = new Cookie("accessToken", newAccessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(30 * 60);
        
        response.addCookie(accessCookie);
        System.out.println("토큰 재발급 완료");

        return ResponseEntity.ok("토큰 재발급 완료");
    }
}