package com.chating.repository.member;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import com.chating.entity.member.Member;

public interface MemberRepository extends JpaRepository<Member, String> {
	// 아이디 중복
	boolean existsByMemId(String memId);

	// 이메일 중복
	boolean existsByEmail(String email);

	// 핸드폰 중복
	boolean existsByPhone(String phone);
	
	// 아이디로 회원 조회
	Optional<Member> findByMemId(@Param("memId") String memId);
	
}
