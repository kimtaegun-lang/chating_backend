package com.chating.service.member;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.chating.common.CustomException;
import com.chating.dto.SignInDTO;
import com.chating.dto.SignUpDTO;
import com.chating.entity.member.Member;
import com.chating.entity.member.RefreshToken;
import com.chating.entity.member.Role;
import com.chating.repository.member.MemberRepository;
import com.chating.repository.refresh.RefreshTokenRepository;
import com.chating.util.JwtUtil;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService{
	private final MemberRepository memberRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final ModelMapper modelMapper;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtUtil jwtUtil;
	
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
	
	// 로그인
	@Transactional
	public Map<String,String> signIn(SignInDTO userData)
	{
	    try {
	        Authentication authentication = authenticationManager.authenticate(
	            new UsernamePasswordAuthenticationToken(
	                userData.getMemId(),
	                userData.getPwd()
	            )
	        );
	        
	        //securityContext에 인증 정보 저장
	        SecurityContextHolder.getContext().setAuthentication(authentication);
	        
	        String accessToken=jwtUtil.generateAccessToken(userData.getMemId());
	        String refreshToken=jwtUtil.generateRefreshToken(userData.getMemId());
	        
	        // Member 조회
	        Member member = memberRepository.findById(userData.getMemId())
	            .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));
	        
	        // 기존 리프레시 토큰 삭제 (하나의 기기만 로그인 유지)
	        refreshTokenRepository.deleteByMemberMemId(userData.getMemId());
	        
	        // 새 리프레시 토큰 저장
	        RefreshToken newRefreshToken = RefreshToken.builder()
	            .member(member)
	            .token(refreshToken)
	            .exiredTime(LocalDateTime.now().plus(7, ChronoUnit.DAYS))
	            .createdTime(LocalDateTime.now())
	            .build();
	        refreshTokenRepository.save(newRefreshToken);
	        
	        // 토큰 반환
	        Map<String, String> tokens = new HashMap<>();
	        tokens.put("accessToken", accessToken);
	        tokens.put("refreshToken", refreshToken);
	        return tokens;
	        
	    } catch (BadCredentialsException e) {
	        throw new CustomException(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다.");
	    }		
	}
}
