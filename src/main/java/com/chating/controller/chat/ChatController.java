package com.chating.controller.chat;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import com.chating.dto.ChatMessageDTO;
import com.chating.service.chat.ChatService;
import com.chating.util.JwtUtil;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ChatController {
	private final ChatService chatService;
	private final JwtUtil jwtUtil;
	
	 @MessageMapping("/send")  // Client가 /app/send 로 보낸 메시지 받음
	    @SendToUser  // 그 Client에게만 보냄 (1대1)
	    public ChatMessageDTO sendMessage(ChatMessageDTO message) {
	     	message.setReceiver(jwtUtil.getLoginId());
	     	chatService.saveMessage(message);
	        return message;  
	    }
}
