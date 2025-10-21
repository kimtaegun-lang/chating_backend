package com.chating.util;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.chating.common.CustomException;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    @Value("${jwt.secret:your-super-secret-key-must-be-at-least-256-bits-long-for-hs256-algorithm}")
    private String secretKey;

    @Value("${jwt.expiration:86400000}")
    private long expirationTime;

    @Value("${jwt.expiration:604800000}")
    private long refreshExpirationTime;
    
    // Token 생성
    public String generateAccessToken(String username) {
        return Jwts.builder()
                .setSubject(username) // 토큰에 저장할 정보
                .setIssuedAt(new Date()) // 발행일
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime)) // 만료시간 
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)), // 서명
                         SignatureAlgorithm.HS256)
                .compact(); // jwt 문자열 생성
    }

    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpirationTime))
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
    }
    
    // Token에서 username 추출
    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // Token 유효성 검증
    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
 // 현재 로그인 한 아이디 반환 (안전 버전)
    public String getLoginId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
        	throw new CustomException(HttpStatus.UNAUTHORIZED,"로그인 되지 않은 상태입니다.");
        }

        return auth.getName();
    }

}