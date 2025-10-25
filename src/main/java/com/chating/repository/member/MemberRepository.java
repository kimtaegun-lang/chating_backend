package com.chating.repository.member;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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

	// 회원 목록 조회 (검색 + 정렬)
    @Query("SELECT m FROM Member m "
            + "WHERE (:search IS NULL OR :search = '' OR :searchType IS NULL OR :searchType = '' OR "
            + " (:searchType = 'name' AND m.name LIKE CONCAT('%', :search, '%')) OR "
            + " (:searchType = 'email' AND m.email LIKE CONCAT('%', :search, '%')) OR "
            + " (:searchType = 'memId' AND m.memId LIKE CONCAT('%', :search, '%'))) "
            + "ORDER BY "
            + "CASE WHEN :sortType = 'desc' THEN m.createdAt END DESC, "
            + "CASE WHEN :sortType = 'asc' THEN m.createdAt END ASC")
    Page<Member> findAllWithSearch(
            @Param("searchType") String searchType, 
            @Param("search") String search,
            @Param("sortType") String sortType, 
            Pageable pageable);

}
