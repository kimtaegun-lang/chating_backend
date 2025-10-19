package com.chating.service.chat;

import java.util.List;

import com.chating.dto.ChatMessageDTO;
import com.chating.entity.member.Chat;

public interface ChatService {
	Chat saveMessage(ChatMessageDTO message); // 메시지 저장
}
