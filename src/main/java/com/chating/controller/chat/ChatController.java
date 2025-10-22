package com.chating.controller.chat;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chating.dto.chat.ChatMessageDTO;
import com.chating.dto.chat.ConversationDTO;
import com.chating.service.chat.ChatService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ChatController {
	private final ChatService chatService;

	// roomId 기반 메시지 전송 (RabbitMQ)
	@MessageMapping("/send")
	public ResponseEntity<String> sendMessage(@Valid ChatMessageDTO message, SimpMessageHeaderAccessor headerAccessor) {
	    String userId = (String) headerAccessor.getSessionAttributes().get("userId");
	    message.setSender(userId);
	    chatService.sendMessage(message);
	    return ResponseEntity.ok("메시지 전송 완료");
	}

	// 대화 내역 조회
	@PreAuthorize("isAuthenticated()")
	@PostMapping("api/chat/getConversation")
	public ResponseEntity<Map<String,Object>> getConversation(@RequestBody @Valid ConversationDTO conversationDTO) {
		 Map<String, Object> response = new HashMap<>();
		    response.put("message", "대화 내역 조회 완료");
		    response.put("data", chatService.getConversation(conversationDTO)); // 실제 대화 데이터
		    return ResponseEntity.ok(response);
	}
	
	// 채팅방 조회
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/api/chat/chatRooms")
	public ResponseEntity<Map<String,Object>> getMyChatRooms(
			@RequestParam("userId") String userId) {
		Map<String,Object> response=new HashMap<>();
		response.put("message", "채팅 목록 조회 완료");
		response.put("data", chatService.getMyChatRooms(userId));
	    return ResponseEntity.ok(response);
	}
}
