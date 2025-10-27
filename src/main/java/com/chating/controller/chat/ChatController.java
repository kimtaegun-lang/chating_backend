package com.chating.controller.chat;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chating.dto.chat.ConversationDTO;
import com.chating.dto.chat.DeleteMessageDTO;
import com.chating.dto.chat.sendMessageDTO;
import com.chating.entity.member.Member;
import com.chating.service.chat.ChatService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ChatController {
	private final ChatService chatService;

	// 메시지 전송
	@MessageMapping("/send")
	public ResponseEntity<String> sendMessage(@Valid sendMessageDTO message) {
	chatService.saveMessage(message);
		return ResponseEntity.ok("메시지 전송 완료");
	}

	// 메시지 삭제
	@MessageMapping("/delete")
	public ResponseEntity<String> deleteChat(@Valid DeleteMessageDTO message) {
		chatService.deleteChat(message);
		return ResponseEntity.ok("메시지가 정상적으로 삭제되었습니다.");
	}

	// 대화 내역 조회
	@PreAuthorize("isAuthenticated()")
	@PostMapping("api/chat/getConversation")
	public ResponseEntity<Map<String, Object>> getConversation(@RequestBody @Valid ConversationDTO conversationDTO) {
		Map<String, Object> response = new HashMap<>();
		response.put("message", "대화 내역 조회 완료");
		response.put("data", chatService.getConversation(conversationDTO)); // 실제 대화 데이터
		return ResponseEntity.ok(response);
	}

	// 채팅방 조회
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/api/chat/chatRooms")
	public ResponseEntity<Map<String, Object>> getMyChatRooms(@RequestParam("userId") String userId) {
		Map<String, Object> response = new HashMap<>();
		response.put("message", "채팅 목록 조회 완료");
		response.put("data", chatService.getMyChatRooms(userId));
		return ResponseEntity.ok(response);
	}
	
	// 상대 측 탈퇴 여부 확인
	@GetMapping("/api/chat/receiver-status")
	public ResponseEntity<Boolean> getReceiverStatus(
	    @RequestParam("receiverId") String receiverId) {
	    boolean isActive=chatService.getReceiverStatus(receiverId);
	    
	    return ResponseEntity.ok(isActive);
	}
}
