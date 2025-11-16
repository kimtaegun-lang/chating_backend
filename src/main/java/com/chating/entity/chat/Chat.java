package com.chating.entity.chat;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Builder
public class Chat implements Serializable{
	//private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long chatId; // 채팅 번호
	
	@Column(nullable=false)
	private String sender;  // 보낸이
	
	@Column(nullable=false)
    private String receiver;  // 받는 사람 
	
	@Column(nullable=false)
	private LocalDateTime createdAt; // 메시지 전송 시간
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Type type; // 메시지 타입
	
	@Column(nullable=true)
	private String content; // 텍스트
	
	@Column(nullable=true)
	private String url; // 이미지 및 파일 url
	
    @Column(nullable=true)
    private String fileName; // 파일 원본 이름
    
    @Column(nullable=true)
    private Long fileSize; // 파일 사이즈
    
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="room_id",nullable=false)
	private ChatRoom chatroom; // 채팅방 번호
}
