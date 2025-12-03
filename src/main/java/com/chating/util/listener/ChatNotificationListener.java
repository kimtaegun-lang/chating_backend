package com.chating.util.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.chating.dto.chat.BroadcastResDTO;
import com.chating.util.chat.UserSessionManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatNotificationListener {
	    private final SimpMessagingTemplate messagingTemplate;
	    private final UserSessionManager sessionManager;

	    @RabbitListener(
	    	    queues = "#{notificationQueue.name}",
	    	    messageConverter = "jackson2JsonMessageConverter"
	    	)
	    	public void handleNotification(BroadcastResDTO message) {
	    	    String targetUserId = message.getReceiver();
	    
	    	    
	    	    if (!sessionManager.isUserOnThisServer(targetUserId)) {
	    	        
	    	        return;
	    	    }
	    	    
	    	    String sessionId = sessionManager.getSessionId(targetUserId);
	    	    if (sessionId == null) {
	    	     
	    	        return;
	    	    }
	    	    
	    	    String destination = "/queue/notify-user" + sessionId;
	    	    
	    	   
	    	    messagingTemplate.convertAndSend(destination, message);
	    	    
	    	    log.info("알림 전송 완료: {}", destination);
	    	}
}