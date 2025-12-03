package com.chating.repository.chat;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.chating.dto.chat.ConversationResDTO;
import com.chating.entity.chat.Chat;
import com.chating.entity.chat.State;

public interface ChatRepository extends JpaRepository<Chat,Long> {
	// 두 회원 간의 메시지 내역 조회
	@Query("""
			SELECT new com.chating.dto.chat.ConversationResDTO(
			    c.chatId,
			    c.sender,
			    c.receiver,
			    c.content,
			    c.createdAt,
			    c.type,
			    f.url,
			    f.fileName,
			    f.fileSize,
			    c.isRead,
			    c.state
			)
			FROM Chat c
			LEFT JOIN c.chatFile f
			WHERE 
			    ((c.sender = :user1 AND c.receiver = :user2)
			    OR (c.sender = :user2 AND c.receiver = :user1))
			    AND c.chatId < :chatId
			""")
			Page<ConversationResDTO> getConversationByChatId(
			        @Param("user1") String user1,
			        @Param("user2") String user2,
			        @Param("chatId") Long chatId,
			        Pageable pageable);

	
	// 업로드 후 30일 지난 파일 불러 오기
	@Query("SELECT c FROM Chat c WHERE (c.type='FILE' OR c.type='IMAGE') AND c.createdAt < :limit")
	List<Chat> findOldFiles(@Param("limit") LocalDateTime limit);
	
	// batch에 사용하는 채팅 상태값 업데이트
	@Modifying
	@Query("UPDATE Chat c SET c.state = :state WHERE c.chatId IN (:chatIds)")
	void deleteChatBatch(@Param("chatIds") List<Long> chatIds,
	                     @Param("state") State state);
	 default void deleteChat(Long chatId, State state) {
	        deleteChatBatch(List.of(chatId), state);
	    }

}
