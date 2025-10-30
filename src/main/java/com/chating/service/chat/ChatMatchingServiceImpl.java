package com.chating.service.chat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
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
	private final RedisTemplate<String, Object> redisTemplate;
	private final RedissonClient redissonClient;
	private static final String WAITING_QUEUE_KEY = "matching:waiting";
	private static final String MATCHED_USERS_KEY = "matching:matched:";

	// 방 생성
	@Override
	public Long createChatRoom(String user1, String user2) {
		if (user1 == null || user1.trim().isEmpty())
			throw new IllegalArgumentException("발신자 정보 필요");
		if (user2 == null || user2.trim().isEmpty())
			throw new IllegalArgumentException("수신자 정보 필요");
		if (user1.equals(user2))
			throw new IllegalArgumentException("자신과는 채팅 불가");

		Optional<ChatRoom> existingRoom = chatRoomRepository.findRoomByIds(user1, user2);
		if (existingRoom.isPresent())
			throw new IllegalArgumentException("두 사용자 간 채팅방 존재");

		ChatRoom newRoom = ChatRoom.builder().user1(user1).user2(user2).createdAt(LocalDateTime.now())
				.chat(new ArrayList<>()).build();

		return chatRoomRepository.save(newRoom).getRoomId();
	}

	// 랜덤 매칭
	@Override
	public void randomMatching(String userId) {
		RLock lock = redissonClient.getLock("chat:matching:lock");

		System.out.println("=== 매칭 요청 수신 ===");
		System.out.println("userId: " + userId);
		try {
			if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
				// Redis 대기자 추가
				redisTemplate.opsForSet().add(WAITING_QUEUE_KEY, userId);

				printWaitingQueue();

				Long queueSize = redisTemplate.opsForSet().size(WAITING_QUEUE_KEY);
				System.out.println("대기 큐 현재 인원: " + queueSize);

				if (queueSize != null && queueSize >= 2) {
					Object user1Obj = redisTemplate.opsForSet().pop(WAITING_QUEUE_KEY);
					Object user2Obj = redisTemplate.opsForSet().pop(WAITING_QUEUE_KEY);

					if (user1Obj != null && user2Obj != null) {
						String user1 = user1Obj.toString();
						String user2 = user2Obj.toString();

						if (user1.equals(user2)) {
							redisTemplate.opsForSet().add(WAITING_QUEUE_KEY, user1);
							return;
						}

						Optional<ChatRoom> existingRoom = chatRoomRepository.findRoomByIds(user1, user2);
						if (existingRoom.isPresent()) {
							redisTemplate.opsForSet().add(WAITING_QUEUE_KEY, user1, user2);

							printWaitingQueue();
							return;
						}

						// 매칭 생성
						int roomId = createChatRoom(user1, user2).intValue();
						redisTemplate.opsForValue().set(MATCHED_USERS_KEY + user1, roomId);
						redisTemplate.opsForValue().set(MATCHED_USERS_KEY + user2, roomId);

						Map<String, Object> response1 = Map.of("matched", true, "roomId", roomId, "receiver", user2);
						Map<String, Object> response2 = Map.of("matched", true, "roomId", roomId, "receiver", user1);

						System.out.println("=== 매칭 성공 ===");
						messagingTemplate.convertAndSend("/queue/match-" + user1, response1);
						messagingTemplate.convertAndSend("/queue/match-" + user2, response2);

						printWaitingQueue();
					}
				} else {
					Map<String, Object> waitingResponse = Map.of("matched", false, "message", "매칭 대기 중...");
					messagingTemplate.convertAndSend("/queue/match-" + userId, waitingResponse);
				}
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (Exception e) {
			// 그 외 예외 상황 처리
			System.err.println("매칭 처리 중 오류 발생: " + userId);
			e.printStackTrace();
		} finally {
			// lock 해제
			lock.unlock();
		}
	}

	// 매칭 취소
	@Override
	public void cancelMatching(String userId) {
		RLock lock = redissonClient.getLock("chat:matching:lock");
		System.out.println("=== 매칭 취소 ===");
		System.out.println("userId: " + userId);
		try {
			if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
				redisTemplate.opsForSet().remove(WAITING_QUEUE_KEY, userId);
				redisTemplate.delete(MATCHED_USERS_KEY + userId);
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} finally {
			// lock 해제
			lock.unlock();
		}
		printWaitingQueue();
	}

	// 현재 대기 큐에 있는 사용자 목록 출력
	private void printWaitingQueue() {
		Set<Object> waitingUsers = redisTemplate.opsForSet().members(WAITING_QUEUE_KEY);
		System.out.println("=== 현재 대기 큐 ===");
		if (waitingUsers == null || waitingUsers.isEmpty()) {
			System.out.println("대기 중인 사용자 없음");
		} else {
			waitingUsers.forEach(user -> System.out.println("- " + user));
		}
		System.out.println("=====================");
	}
}
