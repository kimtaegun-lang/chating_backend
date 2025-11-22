package com.chating.util.chat;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import com.chating.util.jwt.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private final UserSessionManager sessionManager;

    // WebSocket 메시지 전송 전 인터셉트
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        
        switch (accessor.getCommand()) {
            case CONNECT -> handleConnect(accessor);
            case DISCONNECT -> handleDisconnect(accessor);
            case SEND -> validateSend(accessor);
        }
        
        return message;
    }

    // WebSocket 연결 시 JWT 검증 및 세션 등록
    
    private void handleConnect(StompHeaderAccessor accessor) {
        String userId = (String) accessor.getSessionAttributes().get("userId");
        String token = (String) accessor.getSessionAttributes().get("token");
        
        if (userId == null || token == null || !jwtUtil.isTokenValid(token)) {
            throw new IllegalStateException("유효하지 않은 WebSocket 토큰입니다.");
        }
        
        // Principal 설정 (Spring Security 용)
        accessor.setUser(() -> userId);
        
        // Redis에 userId → sessionId 매핑 저장
        sessionManager.register(userId, accessor.getSessionId());
        
    } 
    

    // WebSocket 연결 해제 시 세션 정리
    private void handleDisconnect(StompHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();
        if (sessionId != null) {
            sessionManager.unregister(sessionId);
        }
    }

    // 메시지 전송 시 인증 확인
    private void validateSend(StompHeaderAccessor accessor) {
        String userId = (String) accessor.getSessionAttributes().get("userId");
        if (userId == null) {
            throw new IllegalStateException("WebSocket SEND 요청에 인증이 필요합니다.");
        }
    }
    
    
}