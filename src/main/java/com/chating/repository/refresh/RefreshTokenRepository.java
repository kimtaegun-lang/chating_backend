package com.chating.repository.refresh;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chating.entity.member.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken,Long>{
	// 아이디로 찾기
	Optional<RefreshToken> findByMemberMemId(String memId);
	
	// 토큰으로 찾기
	Optional<RefreshToken> findByToken(String token);
	
	// 로그아웃 시 토큰 삭제
	void deleteByMemberMemId(String memId);
	
	// 해당 아이디에 저장된 refresh 토큰 조회
	List<RefreshToken> findAllByMemberMemId(String memId);
}
