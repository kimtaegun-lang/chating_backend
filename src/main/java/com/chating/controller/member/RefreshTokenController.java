package com.chating.controller.member;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.chating.entity.member.Role;
import com.chating.util.JwtUtil;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class RefreshTokenController {
    
    private final JwtUtil jwtUtil;
    
    @PostMapping("/api/refresh")
    public ResponseEntity<?> refreshAccessToken(@RequestBody Map<String, String> requestBody) {
        String refreshToken = requestBody.get("refreshToken");
        
        if (refreshToken == null || !jwtUtil.isTokenValid(refreshToken)) {
            return ResponseEntity.status(HttpServletResponse.SC_FORBIDDEN)
                .body(Map.of(
                    "error", "REFRESH_TOKEN_EXPIRED",
                    "message", "세션이 만료되었습니다. 다시 로그인해주세요."
                ));
        }
        
        String username = jwtUtil.extractUsername(refreshToken);
        String newAccessToken = jwtUtil.generateAccessToken(username,Role.USER);
        
        System.out.println("토큰 재발급 완료: " + username);
        
        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }
}