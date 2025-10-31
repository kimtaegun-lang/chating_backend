package com.chating.controller.member;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chating.dto.member.MemberInfoDTO;
import com.chating.dto.member.SignInDTO;
import com.chating.dto.member.SignInResDTO;
import com.chating.dto.member.SignUpDTO;
import com.chating.dto.member.UpdateMemberDTO;
import com.chating.service.member.MemberService;
import com.chating.util.JwtUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("member/")
public class MemberController {
	private final MemberService memberService;
	private final JwtUtil jwtUtil;
	// 회원 가입 로직
	@PostMapping("signUp")
	public ResponseEntity<String> signUpUser(@RequestBody @Valid SignUpDTO userData) {
		memberService.signUpUser(userData);
		return ResponseEntity.ok("회원 가입 완료");
	}
	
	// 로그인 로직
	@PostMapping("/signIn")
	public ResponseEntity<String> signIn(
	        @RequestBody @Valid SignInDTO userData,
	        HttpServletResponse response) {
	    // 서비스에서 토큰 반환
	    SignInResDTO dto = memberService.signIn(userData);

	    
	    // 쿠키 생성
	    Cookie accessCookie = new Cookie("accessToken", dto.getAccessToken());
	    accessCookie.setHttpOnly(true);  // JS 접근 불가
	    accessCookie.setSecure(true);    // HTTPS만 허용 
	    accessCookie.setPath("/"); // 사이트 전역에서 쿠키가 자동으로 붙음.
	    accessCookie.setMaxAge(30 * 60 ); // 만료 시간 설정

	    Cookie refreshCookie = new Cookie("refreshToken", dto.getRefreshToken());
	    refreshCookie.setHttpOnly(true);
	    refreshCookie.setSecure(true);
	    refreshCookie.setPath("/");
	    refreshCookie.setMaxAge(7 * 24 * 60 * 60);


	   response.addCookie(accessCookie);
	    response.addCookie(refreshCookie); 

	    return ResponseEntity.ok("로그인이 완료되었습니다.");
	}

	
	// 로그아웃 로직
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/signOut")
	public ResponseEntity<String> signOut(HttpServletResponse response) {

	    memberService.signOut();
	    
	    // accessToken 쿠키 만료
	    Cookie accessCookie = new Cookie("accessToken", null);
	    accessCookie.setHttpOnly(true);
	    accessCookie.setSecure(true);
	    accessCookie.setPath("/");
	    accessCookie.setMaxAge(0); // 즉시 만료

	    // refreshToken 쿠키 만료
	    Cookie refreshCookie = new Cookie("refreshToken", null);
	    refreshCookie.setHttpOnly(true);
	    refreshCookie.setSecure(true);
	    refreshCookie.setPath("/");
	    refreshCookie.setMaxAge(0); // 즉시 만료

	    // 응답에 만료된 쿠키 추가
	    response.addCookie(accessCookie);
	    response.addCookie(refreshCookie);
	    return ResponseEntity.ok("로그아웃이 완료되었습니다.");
	}

	
	
	// 회원 정보 수정
	@PreAuthorize("isAuthenticated()")
	@PutMapping("updateMemberInfo")
	public ResponseEntity<Map<String,Object>> updateMemberInfo(@RequestBody @Valid UpdateMemberDTO updateData) {
		memberService.updateMemberInfo(updateData);
		Map<String,Object> response = new HashMap<>();
		response.put("message", "회원 정보가 수정되었습니다.");
		return ResponseEntity.ok(response);
	}
	
	// 회원 탈퇴
	@PreAuthorize("isAuthenticated()")
	@DeleteMapping("deleteMember")
	public ResponseEntity<Map<String,Object>> deleteMember() {
		String userId = jwtUtil.getLoginId();
		memberService.deleteMember(userId);
		Map<String,Object> response = new HashMap<>();
		response.put("message", "회원 탈퇴가 완료되었습니다.");
		return ResponseEntity.ok(response);
	}
	
	// 회원 검증
	@PreAuthorize("isAuthenticated()")
	@GetMapping("auth/check")
	public ResponseEntity<Map<String,Object>> checkAuth(@CookieValue(name = "accessToken") String token) {
	    // 토큰 검증 + 사용자 정보 반환
	    MemberInfoDTO userInfo = memberService.validateAndGetUserInfo(token);
	    Map<String,Object> response=new HashMap<>();
	    response.put("userInfo", userInfo);
	    response.put("message", "회원 인증 완료");
	    return ResponseEntity.ok(response);
	}
}
