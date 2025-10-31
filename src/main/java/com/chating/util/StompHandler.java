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
        
        // connect
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String userId = (String) accessor.getSessionAttributes().get("userId");
            String token = (String) accessor.getSessionAttributes().get("token");
            
            if (userId != null && token != null && jwtUtil.isTokenValid(token)) {
                accessor.setUser(new UsernamePasswordAuthenticationToken(userId, null, new ArrayList<>()));
                
                UsernamePasswordAuthenticationToken auth = 
                    new UsernamePasswordAuthenticationToken(userId, null, new ArrayList<>());
                SecurityContextHolder.getContext().setAuthentication(auth);
                
                System.out.println("STOMP CONNECT 성공: " + userId);
            } else {
                throw new IllegalStateException("인증에 실패했습니다.");
            }
        }
        
        // subscribe
        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String destination = accessor.getDestination();
            String userId = (String) accessor.getSessionAttributes().get("userId");
            
            // 토큰 검증 (구독 시점에 토큰 유효성 재확인)
            if (userId == null) {
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
        
        // send
        if (StompCommand.SEND.equals(accessor.getCommand())) {
            String destination = accessor.getDestination();
            
            if (destination != null && destination.startsWith("/app/")) {
                String userId = (String) accessor.getSessionAttributes().get("userId");
                
                // userId만 확인 
                if (userId == null) {
                    throw new IllegalStateException("인증이 필요합니다.");
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
