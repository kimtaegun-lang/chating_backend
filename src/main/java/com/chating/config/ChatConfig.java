package com.chating.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.chating.util.StompHandler;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocketMessageBroker // 웹 소켓 활성화
@RequiredArgsConstructor
public class ChatConfig implements WebSocketMessageBrokerConfigurer {
	
	private final StompHandler stompHandler;
	  @Override
	    public void configureMessageBroker(MessageBrokerRegistry config) {
	        // RabbitMQ STOMP 브로커 설정
	        config.enableStompBrokerRelay("/topic", "/queue")  
	                .setRelayHost("localhost")
	                .setRelayPort(61613)
	                .setClientLogin("guest")
	                .setClientPasscode("guest")
	                .setVirtualHost("/");
	        
	        config.setApplicationDestinationPrefixes("/app");
	    }

	    @Override
	    public void registerStompEndpoints(StompEndpointRegistry registry) {
	        registry.addEndpoint("/ws-chat")
	                .setAllowedOrigins("http://localhost:3000", "http://localhost:3001")
	                .withSockJS()
	                .setDisconnectDelay(30000);
	    }

	    @Override
	    public void configureClientInboundChannel(ChannelRegistration registration) {
	        registration.interceptors(stompHandler);
	    }
}
