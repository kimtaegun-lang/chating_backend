package com.chating.entity.member;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name="chat")
@AllArgsConstructor
@NoArgsConstructor
public class Chat implements Serializable{
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long chatId; // 채팅방 번호
	
	@Column(nullable=false)
	private String sender;       // 보낸이
	
	@Column(nullable=false)
    private String receiver;     // 받는 사람 
	
	@Column(nullable=false)
	private LocalDateTime createdAt; // 메시지 전송 시간
	
	@Column(nullable=false)
	private String content;
}
