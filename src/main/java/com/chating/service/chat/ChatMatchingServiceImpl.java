package com.chating.service.chat;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.chating.common.CustomException;
import com.chating.entity.chat.ChatRoom;
import com.chating.repository.chat.ChatRoomRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMatchingServiceImpl implements ChatMatchingService {

    private final ChatRoomRepository chatRoomRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedissonClient redissonClient;
    private static final String WAITING_QUEUE_KEY = "matching:waiting";
    private static final String MATCH_CHANNEL = "match:notifications";

    // 두 유저 간 채팅방 생성
    @Override
    public Long createChatRoom(String user1, String user2) {
        if (user1.equals(user2)) {
            throw new IllegalArgumentException("자신과는 채팅 불가");
        }
        
        Optional<ChatRoom> existingRoom = chatRoomRepository.findRoomByIds(user1, user2);
        if (existingRoom.isPresent()) {
            throw new IllegalArgumentException("채팅방 이미 존재");
        }

        ChatRoom room = ChatRoom.builder()
                .user1(user1)
                .user2(user2)
                .createdAt(LocalDateTime.now())
                .chat(new java.util.ArrayList<>())
                .build();
        
        ChatRoom saved = chatRoomRepository.save(room);
        return saved.getRoomId();
    } 
    
    

    /* 랜덤 매칭 요청 또는 취소 처리
    @Override
    public void randomMatching(String userId,String role) {
    	  if (!"USER".equals(role)) {
    	        publishMatchNotification(userId, Map.of(
    	            "matched", false,
    	            "error", "일반 회원만 매칭을 이용할 수 있습니다."
    	        ));
    	        return;
    	    }

    	
        RLock lock = redissonClient.getLock("chat:matching:lock");
        try {
            if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                Boolean alreadyWaiting = redisTemplate.opsForSet().isMember(WAITING_QUEUE_KEY, userId);
                
                // 이미 대기 중이면 매칭 취소
                if (Boolean.TRUE.equals(alreadyWaiting)) {
                    redisTemplate.opsForSet().remove(WAITING_QUEUE_KEY, userId);
                    publishMatchNotification(userId, Map.of(
                        "matched", false,
                        "message", "매칭 취소됨"
                    ));
                    return;
                }

                // 대기 큐에 추가
                redisTemplate.opsForSet().add(WAITING_QUEUE_KEY, userId);
                Long queueSize = redisTemplate.opsForSet().size(WAITING_QUEUE_KEY);

                // 2명 이상이면 매칭 시도
                if (queueSize != null && queueSize >= 2) {
                    String user1 = redisTemplate.opsForSet().pop(WAITING_QUEUE_KEY).toString();
                    String user2 = redisTemplate.opsForSet().pop(WAITING_QUEUE_KEY).toString();

                    // 같은 유저면 다시 큐에 넣기
                    if (user1.equals(user2)) {
                        redisTemplate.opsForSet().add(WAITING_QUEUE_KEY, user2);
                        publishMatchNotification(user2, Map.of(
                            "matched", false,
                            "message", "매칭 대기 중..."
                        ));
                        return;
                    }

                    // 채팅방 생성 및 매칭 완료 알림
                    Long roomId = createChatRoom(user1, user2);

                    publishMatchNotification(user1, Map.of(
                        "matched", true,
                        "roomId", roomId,
                        "receiver", user2
                    ));
                    
                    publishMatchNotification(user2, Map.of(
                        "matched", true,
                        "roomId", roomId,
                        "receiver", user1
                    ));
                } else {
                    // 대기 중 상태 알림
                    publishMatchNotification(userId, Map.of(
                        "matched", false,
                        "message", "매칭 대기 중..."
                    ));
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("매칭 처리 중 인터럽트 발생", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    } */
    
    @Override
    public void randomMatching(String userId, String role) {
        if (!"USER".equals(role)) {
            publishMatchNotification(userId, Map.of(
                "matched", false,
                "error", "일반 회원만 매칭을 이용할 수 있습니다."
            ));
            return;
        }

        RLock lock = redissonClient.getLock("chat:matching:lock");
        try {
            if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                Boolean alreadyWaiting = redisTemplate.opsForSet().isMember(WAITING_QUEUE_KEY, userId);

                // 이미 대기 중이면 매칭 취소
                if (Boolean.TRUE.equals(alreadyWaiting)) {
                    redisTemplate.opsForSet().remove(WAITING_QUEUE_KEY, userId);
                    publishMatchNotification(userId, Map.of(
                        "matched", false,
                        "message", "매칭 취소됨"
                    ));
                    return;
                }

                // 대기 큐에 추가
                redisTemplate.opsForSet().add(WAITING_QUEUE_KEY, userId);
                Long queueSize = redisTemplate.opsForSet().size(WAITING_QUEUE_KEY);

                // 2명 이상이면 매칭 시도
                if (queueSize != null && queueSize >= 2) {
                    String user1 = redisTemplate.opsForSet().pop(WAITING_QUEUE_KEY).toString();
                    String user2 = redisTemplate.opsForSet().pop(WAITING_QUEUE_KEY).toString();

                    // 같은 유저면 다시 큐에 넣기
                    if (user1.equals(user2)) {
                        redisTemplate.opsForSet().add(WAITING_QUEUE_KEY, user2);
                        publishMatchNotification(user2, Map.of(
                            "matched", false,
                            "message", "매칭 대기 중..."
                        ));
                        return;
                    }

                    // 이미 채팅방이 있는지 확인
                    Optional<ChatRoom> existingRoom = chatRoomRepository.findRoomByIds(user1, user2);
                    
                    if (existingRoom.isPresent()) {
                        // 이미 채팅방이 있으면 둘 다 다시 큐에 넣기
                        log.warn("=== 이미 채팅방 존재, 다시 대기열에 추가: user1={}, user2={}", user1, user2);
                        redisTemplate.opsForSet().add(WAITING_QUEUE_KEY, user1);
                        redisTemplate.opsForSet().add(WAITING_QUEUE_KEY, user2);
                        
                        publishMatchNotification(user1, Map.of(
                            "matched", false,
                            "message", "매칭 대기 중..."
                        ));
                        publishMatchNotification(user2, Map.of(
                            "matched", false,
                            "message", "매칭 대기 중..."
                        ));
                        return;
                    }

                    // 채팅방 생성 및 매칭 완료 알림
                    Long roomId = createChatRoom(user1, user2);

                    publishMatchNotification(user1, Map.of(
                        "matched", true,
                        "roomId", roomId,
                        "receiver", user2
                    ));

                    publishMatchNotification(user2, Map.of(
                        "matched", true,
                        "roomId", roomId,
                        "receiver", user1
                    ));
                } else {
                    // 대기 중 상태 알림
                    publishMatchNotification(userId, Map.of(
                        "matched", false,
                        "message", "매칭 대기 중..."
                    ));
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("매칭 처리 중 인터럽트 발생", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    // 매칭 취소 처리
    @Override
    public void cancelMatching(String userId,String role) {
    	 if(!"USER".equals(role)) {
    	        throw new CustomException(HttpStatus.BAD_REQUEST, "일반 회원만 매칭을 이용 할 수 있습니다.");
    	    }
    	
        RLock lock = redissonClient.getLock("chat:matching:lock");
        try {
            if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                redisTemplate.opsForSet().remove(WAITING_QUEUE_KEY, userId);

                publishMatchNotification(userId, Map.of(
                    "matched", false,
                    "message", "매칭 취소됨"
                ));
                
                log.info("매칭 취소: userId={}", userId);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("매칭 취소 중 인터럽트 발생", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    // Redis Pub/Sub로 매칭 알림 발행 (모든 서버에 브로드캐스트)
    private void publishMatchNotification(String userId, Map<String, Object> data) {
        try {
            Map<String, Object> payload = Map.of(
                "userId", userId,
                "data", data
            );
            redisTemplate.convertAndSend(MATCH_CHANNEL, payload);
        } catch (Exception e) {
        }
    }
}