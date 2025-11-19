package com.chating.service.chat;

import org.springframework.messaging.simp.SimpMessageHeaderAccessor;

public interface ChatMatchingService {
	Long createChatRoom(String sender, String receiver); // 채팅방 생성
	void randomMatching(String userId,String role); // 랜덤 매칭
	void cancelMatching(String userId,String role); // 매칭 취소
}
