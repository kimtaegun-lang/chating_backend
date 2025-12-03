package com.chating.entity.chat;

import jakarta.persistence.Column;

public class File {
	@Column(nullable=true)
	private String url; // 이미지 및 파일 url
	
    @Column(nullable=true)
    private String fileName; // 파일 원본 이름
    
    @Column(nullable=true)
    private Long fileSize; // 파일 사이즈
}
