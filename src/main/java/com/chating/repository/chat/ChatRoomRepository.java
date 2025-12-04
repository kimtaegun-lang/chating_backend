package com.chating.repository.chat;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.chating.entity.chat.ChatRoom;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
	// 회원간 채팅방 존재 여부 확인
	@Query(value = "SELECT * FROM chat_room WHERE (user1 = :user1 AND user2 = :user2) OR (user1 = :user2 AND user2 = :user1)", 
		       nativeQuery = true)
		Optional<ChatRoom> findRoomByIds(@Param("user1") String user1, @Param("user2") String user2);

	
	 // 본인이 포함된 채팅방 조회
	@Query("SELECT c FROM ChatRoom c WHERE c.user1 = :userId OR c.user2 = :userId ORDER BY c.createdAt DESC")
	Page<ChatRoom> findMyChatRooms(Pageable pageable,@Param("userId") String userId);
	

}
