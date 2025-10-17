package com.chating.entity.member;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="member")
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
	
	// 리프레시 토큰 리스트 (여러기기 로그인 고려)
	@OneToMany(mappedBy="member",cascade=CascadeType.REMOVE)
	private List<RefreshToken> refreshTokens;
	
}
