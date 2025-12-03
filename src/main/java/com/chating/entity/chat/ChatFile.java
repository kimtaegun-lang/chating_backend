package com.chating.entity.chat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name="chat_file")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatFile {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long FileId; // 채팅 번호
	
	@OneToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="chat_id",nullable=false)
	private Chat chat;
	
	@Column(nullable=true)
	private String url; // 이미지 및 파일 url
	
    @Column(nullable=true)
    private String fileName; // 파일 원본 이름
    
    @Column(nullable=true)
    private Long fileSize; // 파일 사이즈
}
