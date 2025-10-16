package com.chating.service.member;

import java.time.LocalDateTime;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.chating.common.CustomException;
import com.chating.dto.SignInDTO;
import com.chating.dto.SignUpDTO;
import com.chating.entity.member.Member;
import com.chating.entity.member.Role;
import com.chating.repository.member.MemberRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService{
	private final MemberRepository memberRepository;
	private final ModelMapper modelMapper;
	private final PasswordEncoder passwordEncoder;
	// 회원 가입 로직
	@Transactional
	public void signUpUser(SignUpDTO userData)
	{
		  // 1. 아이디 중복 체크
        if (memberRepository.existsByMemId(userData.getMemId())) {
            throw new CustomException(HttpStatus.BAD_REQUEST,"이미 사용 중인 아이디입니다.");
        }

        // 2. 이메일 중복 체크
        if (memberRepository.existsByEmail(userData.getEmail())) {
            throw new CustomException(HttpStatus.BAD_REQUEST,"이미 사용 중인 이메일입니다.");
        }

        // 3. 전화번호 중복 체크
        if (memberRepository.existsByPhone(userData.getPhone())) {
            throw new CustomException(HttpStatus.BAD_REQUEST,"이미 사용 중인 전화번호입니다.");
        }
        
		// DTO → Entity 자동 변환
        Member member = modelMapper.map(userData, Member.class);
        
        // 비밀번호 암호화
        member.setPwd(passwordEncoder.encode(member.getPwd()));
        
        // 생성일자, Role 설정
        member.setCreatedAt(LocalDateTime.now());
        member.setRole(Role.USER);

        // 저장
        memberRepository.save(member);
	}
	
	@Transactional
	public void signIn(SignInDTO userData)
	{
		Optional<Member> memberOpt = memberRepository.findByMemId(userData.getMemId());
		if (memberOpt.isEmpty()) {
			 throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다.");
		}
		Member member = memberOpt.get();
		if (!passwordEncoder.matches(userData.getPwd(), member.getPwd())) {
			throw new CustomException(HttpStatus.UNAUTHORIZED,"아이디 또는 비밀번호가 올바르지 않습니다.");
		}
		//
		
	}
}
