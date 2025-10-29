package com.chating.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
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
	
	@Bean
	public TaskScheduler taskScheduler() {
		ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
		taskScheduler.setPoolSize(1);
		taskScheduler.setThreadNamePrefix("websocket-heartbeat-");
		taskScheduler.initialize();
		return taskScheduler;
	}
	
	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		// Simple Broker 사용 (RabbitMQ 불필요)
		config.enableSimpleBroker("/topic", "/queue")
			.setTaskScheduler(taskScheduler())
			.setHeartbeatValue(new long[] {20000, 20000});
		
		config.setApplicationDestinationPrefixes("/app");
	}
	
	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		String frontUrl = System.getenv("FRONTEND_URL");
		
		System.out.println("====================================");
		System.out.println("🔌 WebSocket CORS 설정");
		System.out.println("🌐 FRONTEND_URL: " + frontUrl);
		System.out.println("====================================");
		
		registry.addEndpoint("/ws-chat")
			.setAllowedOrigins(frontUrl)
			.withSockJS()
			.setHeartbeatTime(25000);
	}
	
	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(stompHandler);
	}
}