package com.chating.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // 웹 소켓 활성화
public class ChatConfig implements WebSocketMessageBrokerConfigurer {
	@Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 메시지 브로커 설정
		 config.enableSimpleBroker("/topic", "/queue"); // 메시지 경로 등록
        config.setApplicationDestinationPrefixes("/app"); // client 로부터 메시지 받는 경로
        config.setUserDestinationPrefix("/user"); // 받은 메시지를 보낼 경로
    }
    
	// stomp: WebSocket 위에서 메시지를 구조적으로 보내기 위한 규칙
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 클라이언트가 접속할 수 있는 주소 등록
    	// 메시지 전송전 연결하는 경로
    	 registry.addEndpoint("/ws-chat")
    	 .setAllowedOrigins("http://localhost:3000")
         .withSockJS();
    }
}
