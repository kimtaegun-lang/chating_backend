package com.chating.controller.member;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chating.common.CustomException;
import com.chating.entity.member.Member;
import com.chating.entity.member.RefreshToken;
import com.chating.entity.member.Role;
import com.chating.repository.member.MemberRepository;
import com.chating.repository.refresh.RefreshTokenRepository;
import com.chating.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class RefreshTokenController {

    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final MemberRepository memberRepository;
    @PostMapping("/api/refresh")
    public ResponseEntity<Map<String,Object>> refreshAccessToken(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            @CookieValue(name = "accessToken", required = false) String accessToken,
            HttpServletRequest request,
            HttpServletResponse response) {
        
        System.out.println("토큰을 재발급 합니다.");
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

        // AccessToken 남은 시간 체크 (5분 이상 남으면 재발급 차단)
        if (accessToken != null && jwtUtil.isTokenValid(accessToken)) {
            long remaining = jwtUtil.getRemainingTime(accessToken);

            if (remaining > 5 * 60 * 1000) {
            	throw new CustomException(HttpStatus.CONFLICT, 
            		    "유효한 토큰이 이미 존재합니다.");
            }
        }
        
        String userId=jwtUtil.extractUsername(refreshToken);
        Member member=memberRepository.findById(userId).orElseThrow(()->
        new CustomException(HttpStatus.NOT_FOUND,"해당 하는 회원이 없습니다."));
        
     // 새로운 AccessToken 생성
        String newAccessToken = jwtUtil.generateAccessToken(jwtUtil.extractUsername(refreshToken), member.getRole());

        ResponseCookie accessCookie = ResponseCookie.from("accessToken", newAccessToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(30 * 60)
                .sameSite("None")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        // 만료시간 정보 추가
	    Map<String, Object> responseBody = new HashMap<>();
	    responseBody.put("message", "토큰 재발급 완료.");
	    responseBody.put("expiresIn", 1800); // 30분 

	    return ResponseEntity.ok(responseBody);

        
    }
}