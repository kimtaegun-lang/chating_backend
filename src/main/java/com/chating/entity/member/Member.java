package com.chating.entity.member;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="member")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Member {
	@Id
	private String memId; // 아이디
	
	private String pwd; // 비밀번호
	
	private String name; // 이름
	
	@Column(unique=true)
	private String email; // 이메일

	@Column(unique=true)
	private String phone; // 핸드폰 번호
	
	@Enumerated(EnumType.STRING)
	private Gender gender; // 성별
	
	private String addr; // 주소
	
	private LocalDateTime createdAt; // 생성 일자
	
	private LocalDate birth; // 생년월일
	
	@Enumerated(EnumType.STRING)
	private Role role; // 권한
}
