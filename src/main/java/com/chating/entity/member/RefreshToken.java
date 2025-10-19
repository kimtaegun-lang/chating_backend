package com.chating.entity.member;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name="refresh_token")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefreshToken {
	// refresh 토큰 저장소
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long tokenId;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="mem_id",nullable=false)
	private Member member;
	
	@Column(nullable=false)
	private String token;
	
	@Column(nullable=false)
	private LocalDateTime exiredTime;
	
	@Column(nullable=false)
	private LocalDateTime createdTime;
	
	
}
