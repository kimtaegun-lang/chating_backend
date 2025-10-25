package com.chating.service.chat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.chating.entity.chat.ChatRoom;
import com.chating.repository.chat.ChatRoomRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatMatchingServiceImpl implements ChatMatchingService {
	private final ChatRoomRepository chatRoomRepository;
	private final SimpMessagingTemplate messagingTemplate;

	// 대기 큐
	private static final ConcurrentLinkedQueue<String> waitingQueue = new ConcurrentLinkedQueue<>();
	// 매칭된 사용자 관리
	private static final ConcurrentHashMap<String, Integer> matchedUsers = new ConcurrentHashMap<>();

	// 채팅방 생성
	@Override
	public Long createChatRoom(String user1,String user2) {
		if (user1 == null || user2.trim().isEmpty()) {
			throw new IllegalArgumentException("발신자 정보가 필요합니다");
		}
		if (user1 == null || user2.trim().isEmpty()) {
			throw new IllegalArgumentException("수신자 정보가 필요합니다");
		}
		if (user1.equals(user2)) {
			throw new IllegalArgumentException("자신과는 채팅할 수 없습니다");
		}

		Optional<ChatRoom> opt = chatRoomRepository.findRoomByIds(user1, user2);

		if (opt.isPresent()) {
			throw new IllegalArgumentException("두 사용자 간 채팅방이 존재합니다.");
		}

		ChatRoom newRoom = ChatRoom.builder().user1(user1).user2(user2).createdAt(LocalDateTime.now())
				.chat(new ArrayList<>()).build();
		chatRoomRepository.save(newRoom);
	
		return chatRoomRepository.save(newRoom).getRoomId();
	}

	// 랜덤 매칭
	public void randomMatching(String userId) {
		System.out.println("=== 매칭 요청 수신 ===");
		System.out.println("userId: " + userId);

		synchronized (waitingQueue) {
			// 이미 대기 중이면 무시
			if (waitingQueue.contains(userId)) {
				System.out.println("이미 대기 중인 사용자: " + userId);
				return;
			}

			// 대기 큐에 추가
			waitingQueue.add(userId);
			System.out.println("대기 큐 추가. 현재 대기자 수: " + waitingQueue.size());

			// 2명 이상이면 매칭
			if (waitingQueue.size() >= 2) {
				String user1 = waitingQueue.poll();
				String user2 = waitingQueue.poll();
				if (user1 != null && user2 != null && !user1.equals(user2)) {
					int roomId = createChatRoom(user1,user2).intValue(); // 유저들간 채팅방 생성

					// 매칭 정보 저장
					matchedUsers.put(user1, roomId);
					matchedUsers.put(user2, roomId);

					// 응답 데이터 생성
					Map<String, Object> response1 = Map.of("matched", true, "roomId", roomId, "receiver", user2);

					Map<String, Object> response2 = Map.of("matched", true, "roomId", roomId, "receiver", user1);

					System.out.println("=== 매칭 성공 ===");
					System.out.println("User1: " + user1 + " -> " + response1);
					System.out.println("User2: " + user2 + " -> " + response2);

					// 각 사용자에게 매칭 알림
					messagingTemplate.convertAndSend("/queue/match-" + user1, response1);
					messagingTemplate.convertAndSend("/queue/match-" + user2, response2);

				} else {
					System.out.println("매칭 실패: user1=" + user1 + ", user2=" + user2);
				}
			} else {
				// 대기 중 알림
				Map<String, Object> waitingResponse = Map.of("matched", false, "message", "매칭 대기 중...");

				System.out.println("대기 중 알림 전송: " + userId);
				messagingTemplate.convertAndSend("/queue/match-" + userId, waitingResponse);
			}
		}
	}
	
	// 매칭 취소
	public void cancelMatching(String userId)
	{
		 System.out.println("=== 매칭 취소 ===");
	        System.out.println("userId: " + userId);        
	        waitingQueue.remove(userId);
	        matchedUsers.remove(userId);
	}
}
