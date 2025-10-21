package com.chating.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConversationDTO {
	private String user1; // 회원 아이디1
	private String user2; // 회원 아이디2 
	private int limit; // 한번에 로드한 메시지 갯수
	private int chatId; // 채팅 id
}
