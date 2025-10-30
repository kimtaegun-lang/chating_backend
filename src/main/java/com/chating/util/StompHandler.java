package com.chating.util;

import java.util.ArrayList;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {
    private final JwtUtil jwtUtil;
    
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        
        // CONNECT: 토큰 검증 및 세션 저장
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            // Handshake에서 저장한 userId와 token 가져오기
            String userId = (String) accessor.getSessionAttributes().get("userId");
            String token = (String) accessor.getSessionAttributes().get("token");
            
            if (userId != null && token != null) {
                accessor.setUser(new UsernamePasswordAuthenticationToken(userId, null, new ArrayList<>()));
                
                UsernamePasswordAuthenticationToken auth = 
                    new UsernamePasswordAuthenticationToken(userId, null, new ArrayList<>());
                SecurityContextHolder.getContext().setAuthentication(auth);
                
                System.out.println("STOMP CONNECT 성공: " + userId);
            } else {
                System.out.println("STOMP CONNECT 실패: Handshake에서 인증 정보 없음");
            }
        }
        
        // SUBSCRIBE: 구독 권한 검증
        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String destination = accessor.getDestination();
            String userId = (String) accessor.getSessionAttributes().get("userId");
            String token = (String) accessor.getSessionAttributes().get("token");
            
            // 인증 체크
            if (token == null || userId == null || !jwtUtil.isTokenValid(token)) {
                throw new IllegalStateException("인증이 필요합니다.");
            }
            
            // 구독 권한 체크
            if (destination != null) {
                if (destination.startsWith("/queue/match-") 
                    && !destination.equals("/queue/match-" + userId)) {
                    throw new IllegalStateException("다른 사용자의 큐를 구독할 수 없습니다.");
                }
            }
        }
        
        // SEND: 메시지 전송 권한 검증
        if (StompCommand.SEND.equals(accessor.getCommand())) {
            String destination = accessor.getDestination();
            
            // /app/로 시작하는 모든 메시지는 인증 필요
            if (destination != null && destination.startsWith("/app/")) {
                String token = (String) accessor.getSessionAttributes().get("token");
                String userId = (String) accessor.getSessionAttributes().get("userId");
                
                // 토큰과 userId 모두 검증
                if (token == null || userId == null || !jwtUtil.isTokenValid(token)) {
                    throw new IllegalStateException("인증이 필요합니다.");
                }
                
                // 토큰에서 추출한 userId와 세션의 userId가 일치하는지 확인
                String tokenUserId = jwtUtil.extractUsername(token);
                if (!userId.equals(tokenUserId)) {
                    throw new IllegalStateException("인증 정보가 일치하지 않습니다.");
                }
                
                // SecurityContext 설정
                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    UsernamePasswordAuthenticationToken auth = 
                        new UsernamePasswordAuthenticationToken(userId, null, new ArrayList<>());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        }
        
        return message;
    }
}