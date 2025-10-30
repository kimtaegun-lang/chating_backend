package com.chating.config;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.chating.util.JwtUtil;
import com.chating.util.StompHandler;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class ChatConfig implements WebSocketMessageBrokerConfigurer {
    
    @Value("${FRONTEND_URL}")
    private String frontUrl;
    
    private final StompHandler stompHandler;
    private final JwtUtil jwtUtil;
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
    }
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-chat")
            .setAllowedOrigins(frontUrl)
            .addInterceptors(new CookieHandshakeInterceptor())  // ← 추가
            .withSockJS()
            .setHeartbeatTime(25000);
    }
    
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompHandler);
    }
    
    // Handshake 시점에 쿠키에서 토큰 추출하여 WebSocket 세션에 저장
    private class CookieHandshakeInterceptor implements HandshakeInterceptor {
        
        @Override
        public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                     WebSocketHandler wsHandler, Map<String, Object> attributes) {
            
            if (request instanceof ServletServerHttpRequest) {
                HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
                
                // HTTP 요청에서 쿠키 읽기
                Cookie[] cookies = servletRequest.getCookies();
                if (cookies != null) {
                    for (Cookie cookie : cookies) {
                        if ("accessToken".equals(cookie.getName())) {
                            String token = cookie.getValue();
                            
                            // 토큰 검증
                            if (jwtUtil.isTokenValid(token)) {
                                String userId = jwtUtil.extractUsername(token);
                                
                                // WebSocket 세션 속성에 저장 (StompHandler에서 사용)
                                attributes.put("userId", userId);
                                attributes.put("token", token);
                                
                                System.out.println("WebSocket Handshake 성공 - userId: " + userId);
                            } else {
                                System.out.println("WebSocket Handshake - 유효하지 않은 토큰");
                            }
                            break;
                        }
                    }
                }
            }
            
            return true;  // 인증 실패해도 연결은 허용 (StompHandler에서 추가 검증)
        }

        @Override
        public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                 WebSocketHandler wsHandler, Exception exception) {
            // Handshake 완료 후 처리 (필요시 구현)
        }
    }
}