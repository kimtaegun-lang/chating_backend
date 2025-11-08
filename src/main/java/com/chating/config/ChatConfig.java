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
            .setAllowedOrigins(frontUrl,"http://localhost:3001")
            .addInterceptors(new CookieHandshakeInterceptor())  
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
    	                        
    	                        //  토큰 검증은 하되, 만료되어도 userId는 추출
    	                        if (jwtUtil.isTokenValid(token)) {
    	                            String userId = jwtUtil.extractUsername(token);
    	                            attributes.put("userId", userId);
    	                            attributes.put("token", token);
    	                            System.out.println("WebSocket Handshake 성공 - userId: " + userId);
    	                        } else {
    	                            //  만료된 토큰이어도 userId는 추출 가능 (서명만 검증)
    	                            try {
    	                                String userId = jwtUtil.extractUsername(token);
    	                                attributes.put("userId", userId);
    	                                attributes.put("token", token);
    	                                System.out.println("WebSocket Handshake - 토큰 만료됨, StompHandler에서 재검증: " + userId);
    	                            } catch (Exception e) {
    	                                System.out.println("WebSocket Handshake - 토큰 파싱 실패");
    	                            }
    	                        }
    	                        break;
    	                    }
    	                }
    	            }
    	        }
    	        
    	        return true;  // 연결 허용, CONNECT에서 최종 검증
    	    }

        @Override
        public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                 WebSocketHandler wsHandler, Exception exception) {
            // Handshake 완료 후 처리
        }
    }
}