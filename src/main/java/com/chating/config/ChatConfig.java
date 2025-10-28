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
	String rabbitHost = System.getenv("RABBITMQ_HOST");
	int rabbitPort = Integer.parseInt(System.getenv("RABBITMQ_PORT"));
	String rabbitUser = System.getenv("RABBITMQ_USERNAME");
	String rabbitPass = System.getenv("RABBITMQ_PASSWORD");
	String frontUrl = System.getenv("FRONTEND_URL");
	  @Override
	    public void configureMessageBroker(MessageBrokerRegistry config) {
	        // RabbitMQ STOMP 브로커 설정
		  /*
	        config.enableStompBrokerRelay("/topic", "/queue")
	                .setRelayHost("localhost")
	                .setRelayPort(61613)
	                .setClientLogin("guest")
	                .setClientPasscode("guest")
	                .setVirtualHost("/"); */
		  config.enableStompBrokerRelay("/topic", "/queue")
	      .setRelayHost(rabbitHost)
	      .setRelayPort(rabbitPort)
	      .setClientLogin(rabbitUser)
	      .setClientPasscode(rabbitPass)
	      .setVirtualHost("/");
	        
	        config.setApplicationDestinationPrefixes("/app");
	    }

	    @Override
	    public void registerStompEndpoints(StompEndpointRegistry registry) {
	        registry.addEndpoint("/ws-chat")
	                .setAllowedOrigins(frontUrl)
	                .withSockJS()
	                .setDisconnectDelay(30000);
	    }

	    @Override
	    public void configureClientInboundChannel(ChannelRegistration registration) {
	        registration.interceptors(stompHandler);
	    }
}
