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
        
        // CONNECT - 토큰 검증
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            validateAndAuthenticateConnection(accessor);
        }
        
        // SUBSCRIBE - userId만 확인
        else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            validateSubscription(accessor);
        }
        
        // SEND - userId만 확인
        else if (StompCommand.SEND.equals(accessor.getCommand())) {
            validateSend(accessor);
        }
        
        return message;
    }
    
    private void validateAndAuthenticateConnection(StompHeaderAccessor accessor) {
        String userId = (String) accessor.getSessionAttributes().get("userId");
        String token = (String) accessor.getSessionAttributes().get("token");
        
        if (userId == null || token == null) {
            throw new IllegalStateException("userId와 token이 필요합니다.");
        }
        
        if (!jwtUtil.isTokenValid(token)) {
            throw new IllegalStateException("유효하지 않은 토큰입니다.");
        }
        
        // 인증 객체 설정
        UsernamePasswordAuthenticationToken auth = 
            new UsernamePasswordAuthenticationToken(userId, null, new ArrayList<>());
        accessor.setUser(auth);
        SecurityContextHolder.getContext().setAuthentication(auth);
        
        System.out.println("STOMP CONNECT 성공: " + userId);
    }
    
    private void validateSubscription(StompHeaderAccessor accessor) {
        String userId = getUserIdFromSession(accessor);
        String destination = accessor.getDestination();
        
        if (destination != null && destination.startsWith("/queue/match-")) {
            if (!destination.equals("/queue/match-" + userId)) {
                throw new IllegalStateException("다른 사용자의 큐를 구독할 수 없습니다.");
            }
        }
    }
    
    private void validateSend(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        
        if (destination != null && destination.startsWith("/app/")) {
            getUserIdFromSession(accessor); // userId 존재 여부만 확인
        }
    }
    
    private String getUserIdFromSession(StompHeaderAccessor accessor) {
        String userId = (String) accessor.getSessionAttributes().get("userId");
        if (userId == null) {
            throw new IllegalStateException("인증이 필요합니다.");
        }
        return userId;
    }
}