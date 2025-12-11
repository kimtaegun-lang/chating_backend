package com.chating.service.chat;


import java.util.List;

import com.chating.dto.chat.ChatRoomResDTO;
import com.chating.dto.chat.ConversationDTO;
import com.chating.dto.chat.ConversationResDTO;
import com.chating.dto.chat.DeleteMessageDTO;
import com.chating.dto.chat.sendMessageDTO;
import com.chating.dto.common.PageResponseDTO;

public interface ChatService {
	void saveMessage(sendMessageDTO message); // 메시지 저장
	void deleteChat(DeleteMessageDTO message); // 메시지 삭제
	List<ConversationResDTO> getConversation(ConversationDTO conversationDTO); // 채팅 내역 조회
	PageResponseDTO<ChatRoomResDTO> getMyChatRooms(String userId,int pageCount,int size); // 본인 채팅방 목록 조회
	boolean getReceiverStatus(String receiverId); // 수신자 측 회원 탈퇴 여부 확인
}
