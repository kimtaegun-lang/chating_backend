package com.chating.controller.chat;

import java.util.List;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chating.dto.chat.ChatMessageDTO;
import com.chating.dto.chat.ChatMessageResDTO;
import com.chating.dto.chat.ChatRoomResDTO;
import com.chating.dto.chat.ConversationDTO;
import com.chating.dto.chat.ConversationResDTO;
import com.chating.dto.common.PageResponseDTO;
import com.chating.entity.chat.ChatRoom;
import com.chating.service.chat.ChatService;
import com.chating.util.JwtUtil;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ChatController {
	private final ChatService chatService;
	private final JwtUtil jwtUtil;
	private final SimpMessagingTemplate messagingTemplate;


	// roomId 기반 메시지 전송 (RabbitMQ)
	@MessageMapping("/send")
	public void sendMessage(ChatMessageDTO message, SimpMessageHeaderAccessor headerAccessor) {
	    String userId = (String) headerAccessor.getSessionAttributes().get("userId");
	    message.setSender(userId);
	    
	    ChatMessageResDTO savedChat = chatService.saveMessage(message);
	    System.out.println("저장된 메시지: " + savedChat);
	    
	    Long roomId = message.getRoomId();
	    
	    messagingTemplate.convertAndSend("/queue/chatroom-" + roomId, savedChat);
	}

	// 채팅방 생성
	@PostMapping("/api/createRoom")
	public Long getChatRoom(@RequestParam String sender, @RequestParam String receiver) {
		ChatRoom chatRoom = chatService.createChatRoom(sender, receiver);
		return chatRoom.getRoomId();
	}

	// 대화 내역 조회
	@PostMapping("api/chat/getConversation")
	public PageResponseDTO<ConversationResDTO> getConversation(@RequestBody ConversationDTO conversationDTO) {
		
		return chatService.getConversation(conversationDTO);
	}
	
	// 채팅방 조회
	@GetMapping("/api/chat/chatRooms")
	public List<ChatRoomResDTO> getMyChatRooms(
			@RequestParam("userId") String userId) {
	    return chatService.getMyChatRooms(userId);
	}
}
