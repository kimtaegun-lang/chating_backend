package com.chating.controller.chat;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chating.dto.chat.ConversationDTO;
import com.chating.dto.chat.DeleteMessageDTO;
import com.chating.dto.chat.sendMessageDTO;
import com.chating.entity.chat.ChatRoom;
import com.chating.service.chat.ChatService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ChatController {
	private final ChatService chatService;

	// 메시지 전송
	@MessageMapping("/send/message")
	public ResponseEntity<String> sendMessage(@Valid sendMessageDTO message) {
		chatService.saveMessage(message);
		return ResponseEntity.ok("메시지 전송 완료");
	}

	// HTTP 엔드포인트로 파일 업로드
	@PostMapping("api/send/file")
	public ResponseEntity<String> sendFile(@ModelAttribute sendMessageDTO message) {
	    chatService.saveMessage(message);  // 내부에서 WebSocket 브로드캐스트
	    return ResponseEntity.ok("파일 전송 완료");
	}

	// 메시지 삭제
	@MessageMapping("/delete")
	public ResponseEntity<String> deleteChat(@Valid DeleteMessageDTO message) {
		chatService.deleteChat(message);
		return ResponseEntity.ok("메시지가 정상적으로 삭제되었습니다.");
	}

	// 대화 내역 조회
	@PreAuthorize("isAuthenticated()")
	@GetMapping("api/chat/getConversation")
	public ResponseEntity<Map<String, Object>> getConversation(@Valid ConversationDTO conversationDTO) {
		Map<String, Object> response = new HashMap<>();
		response.put("message", "대화 내역 조회 완료");
		response.put("data", chatService.getConversation(conversationDTO)); // 실제 대화 데이터
		return ResponseEntity.ok(response);
	}

	// 채팅방 조회
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/api/chat/chatRooms")
	public ResponseEntity<Map<String, Object>> getMyChatRooms(@RequestParam("userId") String userId,
			@RequestParam("size") int size, @RequestParam("pageCount") int pageCount) {
		Map<String, Object> response = new HashMap<>();
		response.put("message", "채팅 목록 조회 완료");
		response.put("data", chatService.getMyChatRooms(userId, pageCount, size));
		return ResponseEntity.ok(response);
	}

	// 상대 측 탈퇴 여부 확인
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/api/chat/receiver-status")
	public ResponseEntity<Boolean> getReceiverStatus(@RequestParam("receiverId") String receiverId) {
		boolean isActive = chatService.getReceiverStatus(receiverId);

		return ResponseEntity.ok(isActive);
	}
	
}
