package com.chating.repository.chat;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.chating.entity.member.Chat;

public interface ChatRepository extends JpaRepository<Chat,Long> {
	// 두 회원 간의 메시지 내역 조회
	@Query("SELECT c FROM Chat c WHERE (c.sender = :user1 AND c.receiver = :user2) OR (c.sender = :user2 AND c.receiver = :user1) ORDER BY c.createdAt ASC")
    List<Chat> getConversation(@Param("user1") String user1, @Param("user2") String user2);
}
