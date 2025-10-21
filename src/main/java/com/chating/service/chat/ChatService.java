package com.chating.service.chat;

import java.util.List;

import com.chating.dto.chat.ChatMessageDTO;
import com.chating.dto.chat.ChatMessageResDTO;
import com.chating.dto.chat.ChatRoomResDTO;
import com.chating.dto.chat.ConversationDTO;
import com.chating.dto.chat.ConversationResDTO;
import com.chating.dto.common.PageResponseDTO;
import com.chating.entity.chat.Chat;
import com.chating.entity.chat.ChatRoom;

public interface ChatService {
	ChatMessageResDTO saveMessage(ChatMessageDTO message); // 메시지 저장
	ChatRoom createChatRoom(String sender, String receiver); // 채팅방 생성
	PageResponseDTO<ConversationResDTO> getConversation(ConversationDTO conversationDTO); // 채팅 내역 조회
	List<ChatRoomResDTO> getMyChatRooms(String userId); // 본인 채팅방 목록 조회
}
