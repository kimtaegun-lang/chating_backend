package com.chating.controller.chat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.RestController;

import com.chating.dto.ChatMessageDTO;
import com.chating.entity.member.Chat;
import com.chating.service.chat.ChatService;
import com.chating.util.JwtUtil;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ChatController {
	private final ChatService chatService;
	private final JwtUtil jwtUtil;
	private final SimpMessagingTemplate messagingTemplate;

	@MessageMapping("/send")
	public void sendMessage(ChatMessageDTO message, SimpMessageHeaderAccessor headerAccessor) {
	    String userId = (String) headerAccessor.getSessionAttributes().get("userId");
	    message.setSender(userId);
	    Chat savedChat = chatService.saveMessage(message);
	    
	    // /queue로 직접 전송 (RabbitMQ가 이해하는 경로)
	    messagingTemplate.convertAndSend(
	        "/queue/" + message.getReceiver(),
	        savedChat
	    );
	    
	    messagingTemplate.convertAndSend(
	        "/queue/" + userId,
	        savedChat
	    );
	}
}
