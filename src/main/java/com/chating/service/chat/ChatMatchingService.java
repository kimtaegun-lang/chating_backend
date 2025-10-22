package com.chating.service.chat;

public interface ChatMatchingService {
	Long createChatRoom(String sender, String receiver); // 채팅방 생성
	void randomMatching(String userId); // 랜덤 매칭
	void cancelMatching(String userId); // 매칭 취소
}
