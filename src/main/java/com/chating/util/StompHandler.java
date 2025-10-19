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
	// 웹 소켓 요청일 경우 실행됨.
    private final JwtUtil jwtUtil;
    
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                
                if (jwtUtil.isTokenValid(token)) {
                    String userId = jwtUtil.extractUsername(token);
                    
                    // 웹 소켓 셰션 단위 저장
                    accessor.getSessionAttributes().put("userId", userId);
                    accessor.setUser(new UsernamePasswordAuthenticationToken(userId, null, new ArrayList<>()));
                    // securitycontext에 저장하는 이유는 이후 권한 처리를 위해
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userId, null, new ArrayList<>());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    System.out.println("STOMP user: " + accessor.getUser().getName());
                    System.out.println("WebSocket 인증 성공: " + userId);
                }
            }
        }
        
        return message;
    }
}
