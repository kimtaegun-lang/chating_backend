package com.chating.controller.member;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chating.common.CustomException;
import com.chating.entity.member.RefreshToken;
import com.chating.entity.member.Role;
import com.chating.repository.refresh.RefreshTokenRepository;
import com.chating.util.JwtUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class RefreshTokenController {

    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    // IP별 마지막 요청 시간 저장
    private final Map<String, Long> lastRequestTime = new ConcurrentHashMap<>();

    @PostMapping("/api/refresh")
    public ResponseEntity<String> refreshAccessToken(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletRequest request,
            HttpServletResponse response) {
    	
        String ip = request.getRemoteAddr();
        long currentTime = System.currentTimeMillis();
        
        Long lastTime = lastRequestTime.get(ip);
        
        if (lastTime != null && currentTime - lastTime < 1000) {
            throw new CustomException(HttpStatus.TOO_MANY_REQUESTS, "너무 많은 요청입니다. 잠시 후 다시 시도해주세요.");
        }
        
        lastRequestTime.put(ip, currentTime);

        
        
        // RefreshToken 검증
        if (refreshToken == null || !jwtUtil.isTokenValid(refreshToken)) {
            throw new CustomException(HttpStatus.FORBIDDEN, "세션이 만료되었습니다. 다시 로그인 해주세요.");
        } 
        
     // DB에서 토큰 확인
        RefreshToken tokenEntity = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new CustomException(HttpStatus.FORBIDDEN,
                        "세션이 만료되었습니다. 다시 로그인 해주세요."));

        // 만료시간 체크
        if (tokenEntity.getExiredTime().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(tokenEntity); // 만료 토큰 삭제
            throw new CustomException(HttpStatus.FORBIDDEN,
                    "세션이 만료되었습니다. 다시 로그인 해주세요.");
        }

        
 
        // 새로운 AccessToken 생성
        String newAccessToken = jwtUtil.generateAccessToken(jwtUtil.extractUsername(refreshToken), Role.USER);

        Cookie accessCookie = new Cookie("accessToken", newAccessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(30 * 60);

        response.addCookie(accessCookie);
        System.out.println("토큰 재발급 완료 - IP: " + ip);

        return ResponseEntity.ok("토큰 재발급 완료");
    }
}