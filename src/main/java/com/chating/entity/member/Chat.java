package com.chating.entity.member;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name="chat")
@AllArgsConstructor
@NoArgsConstructor
public class Chat {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long chatId; // 채팅방 번호
	private String sender;       // 보낸이
    private String receiver;     // 받는 사람 
	private LocalDateTime createdAt; // 메시지 전송 시간
}
