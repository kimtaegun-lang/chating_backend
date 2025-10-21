package com.chating.repository.chat;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.chating.dto.chat.ConversationResDTO;
import com.chating.entity.chat.Chat;

public interface ChatRepository extends JpaRepository<Chat,Long> {
	// 두 회원 간의 메시지 내역 조회
	@Query("SELECT new com.chating.dto.chat.ConversationResDTO(c.sender, c.receiver, c.createdAt, c.content, c.chatId) " +
		       "FROM Chat c " +
		       "WHERE ((c.sender = :user1 AND c.receiver = :user2) OR (c.sender = :user2 AND c.receiver = :user1)) " +
		       "AND c.chatId < :chatId")
		Page<ConversationResDTO> getConversationByChatId(
		    @Param("user1") String user1,
		    @Param("user2") String user2,
		    @Param("chatId") Long chatId,
		    Pageable pageable
		);


}
