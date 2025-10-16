package com.chating.service.chat;

import java.util.List;

import com.chating.dto.ChatMessageDTO;
import com.chating.entity.member.Chat;

public interface ChatService {
	void saveMessage(ChatMessageDTO message); // 메시지 저장
	List<Chat>getConversation(String user1, String user2); // 메시지 조회
}
