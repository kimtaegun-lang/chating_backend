package com.chating.service.member;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

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
import com.chating.dto.member.MemberInfoDTO;
import com.chating.dto.member.SignInDTO;
import com.chating.dto.member.SignInResDTO;
import com.chating.dto.member.SignUpDTO;
import com.chating.dto.member.UpdateMemberDTO;
import com.chating.entity.member.Member;
import com.chating.entity.member.RefreshToken;
import com.chating.entity.member.Role;
import com.chating.entity.member.Status;
import com.chating.repository.member.MemberRepository;
import com.chating.repository.refresh.RefreshTokenRepository;
import com.chating.util.JwtUtil;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
	private final MemberRepository memberRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final ModelMapper modelMapper;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtUtil jwtUtil;

	// 회원 가입 로직
	@Transactional
	@Override
	public void signUpUser(SignUpDTO userData) {
		// 아이디 중복 체크
		if (memberRepository.existsByMemId(userData.getMemId())) {
			throw new CustomException(HttpStatus.BAD_REQUEST, "이미 사용 중인 아이디입니다.");
		}

		// 이메일 중복 체크
		if (memberRepository.existsByEmail(userData.getEmail())) {
			throw new CustomException(HttpStatus.BAD_REQUEST, "이미 사용 중인 이메일입니다.");
		}

		// 전화번호 중복 체크
		if (memberRepository.existsByPhone(userData.getPhone())) {
			throw new CustomException(HttpStatus.BAD_REQUEST, "이미 사용 중인 전화번호입니다.");
		}

		Member member = modelMapper.map(userData, Member.class);

		// 비밀번호 암호화
		member.setPwd(passwordEncoder.encode(member.getPwd()));

		// 생성일자, Role 설정
		member.setCreatedAt(LocalDateTime.now());
		member.setRole(Role.USER);
		member.setStatus(Status.ACTIVE);
		// 저장
		memberRepository.save(member);
	}

	
	 // 로그인
	@Transactional
	@Override
	public SignInResDTO signIn(SignInDTO userData) {
	    Member member = memberRepository.findById(userData.getMemId())
	            .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "아이디가 일치하지 않습니다."));

	    if (member.getStatus() == Status.BANNED) {
	        throw new CustomException(HttpStatus.FORBIDDEN, "이 계정은 이용이 정지되었습니다.");
	    }


	    try {
	        Authentication authentication = authenticationManager.authenticate(
	                new UsernamePasswordAuthenticationToken(userData.getMemId(), userData.getPwd())
	        );
	        SecurityContextHolder.getContext().setAuthentication(authentication);
	    } catch (BadCredentialsException e) {
	        throw new CustomException(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다.");
	    }

	    String accessToken = jwtUtil.generateAccessToken(userData.getMemId(), member.getRole());
	    String refreshToken = jwtUtil.generateRefreshToken(userData.getMemId());

	    // refresh token 관리
	    List<RefreshToken> existingTokens = refreshTokenRepository.findAllByMemberMemId(userData.getMemId());
	    if (existingTokens.size() >= 5) {
	        RefreshToken oldest = existingTokens.stream()
	                .min(Comparator.comparing(RefreshToken::getCreatedTime))
	                .orElseThrow();
	        refreshTokenRepository.delete(oldest);
	    }

	    refreshTokenRepository.save(
	        RefreshToken.builder()
	            .member(member)
	            .token(refreshToken)
	            .exiredTime(LocalDateTime.now().plusDays(7))
	            .createdTime(LocalDateTime.now())
	            .build()
	    );

	    return new SignInResDTO(accessToken,refreshToken);
	}
	
	// 로그 아웃
	@Transactional
	@Override
	public void signOut() {
		String userId = jwtUtil.getLoginId();
		Member member = memberRepository.findById(userId)
				.orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));
		refreshTokenRepository.deleteByMemberMemId(member.getMemId());
	}


	// 회원 정보 수정
	@Transactional
	@Override
	public void updateMemberInfo(UpdateMemberDTO updateData) {
		String userId = jwtUtil.getLoginId();
		Member member = memberRepository.findById(userId)
				.orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));

		// 이메일 변경 시 중복 체크
		if (updateData.getEmail() != null && !updateData.getEmail().equals(member.getEmail())) {
			if (memberRepository.existsByEmail(updateData.getEmail())) {
				throw new CustomException(HttpStatus.BAD_REQUEST, "이미 사용 중인 이메일입니다.");
			}
			member.setEmail(updateData.getEmail());
		}

		// 전화번호 변경 시 중복 체크
		if (updateData.getPhone() != null && !updateData.getPhone().equals(member.getPhone())) {
			if (memberRepository.existsByPhone(updateData.getPhone())) {
				throw new CustomException(HttpStatus.BAD_REQUEST, "이미 사용 중인 전화번호입니다.");
			}
			member.setPhone(updateData.getPhone());
		}

		// 주소 변경
		if (updateData.getAddr() != null) {
			member.setAddr(updateData.getAddr());
		}

		// 비밀번호 변경
		if (updateData.getNewPwd() != null && !updateData.getNewPwd().isEmpty()) {
			// 현재 비밀번호 확인
			if (updateData.getCurrentPwd() == null || updateData.getCurrentPwd().isEmpty()) {
				throw new CustomException(HttpStatus.BAD_REQUEST, "현재 비밀번호를 입력해주세요.");
			}

			if (!passwordEncoder.matches(updateData.getCurrentPwd(), member.getPwd())) {
				throw new CustomException(HttpStatus.UNAUTHORIZED, "현재 비밀번호가 일치하지 않습니다.");
			}

			member.setPwd(passwordEncoder.encode(updateData.getNewPwd()));
		}

		memberRepository.save(member);
	}

	// 회원 탈퇴
	@Transactional
	@Override
	public void deleteMember(String userId) {

		Member member = memberRepository.findById(userId)
				.orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));

		// Refresh Token 삭제
		refreshTokenRepository.deleteByMemberMemId(member.getMemId());

		// 회원 삭제
		memberRepository.delete(member);

		// SecurityContext 초기화
		SecurityContextHolder.clearContext();
	}
	
	 // accesstoken 검증 및 회원 정보 전달
	@Transactional
	@Override
	 public MemberInfoDTO validateAndGetUserInfo(String accessToken) {
		if(!jwtUtil.isTokenValid(accessToken))
		{
			throw new CustomException(HttpStatus.UNAUTHORIZED,"정보가 올바르지 않습니다. 다시 로그인해주세요.");
		}
		String memberId=jwtUtil.getLoginId();
		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));
		
		MemberInfoDTO memberInfo=modelMapper.map(member,MemberInfoDTO.class);
		return memberInfo;
	}
}
