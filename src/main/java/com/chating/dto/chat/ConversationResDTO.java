package com.chating.dto.chat;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConversationResDTO { // 채팅 내역 res dto
	private String sender; // 회원 아이디1
	 private String receiver; // 회원 아이디2 
	private LocalDateTime createdAt; // 메시지 전송 시간
	private String content; // 메시지
	private Long chatId; // 채팅 번호
}
