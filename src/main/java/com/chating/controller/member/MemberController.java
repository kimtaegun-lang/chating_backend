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
	    
	    SignInResDTO dto = memberService.signIn(userData);

	    // Access Token 쿠키
	    ResponseCookie accessCookie = ResponseCookie.from("accessToken", dto.getAccessToken())
	            .httpOnly(true)
	            .secure(true)
	            .path("/")
	            .maxAge(30 * 60)
	            .sameSite("None")
	            .build();

	    // Refresh Token 쿠키
	    ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", dto.getRefreshToken())
	            .httpOnly(true)
	            .secure(true)
	            .path("/")
	            .maxAge(7 * 24 * 60 * 60)
	            .sameSite("None") 
	            .build();

	    response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
	    response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

	    return ResponseEntity.ok("로그인이 완료되었습니다.");
	}

	
	// 로그아웃 로직
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/signOut")
	public ResponseEntity<String> signOut(HttpServletResponse response) {
	    memberService.signOut();
	    
	    // Access Token 쿠키 만료
	    ResponseCookie accessCookie = ResponseCookie.from("accessToken", "")
	            .httpOnly(true)
	            .secure(true)
	            .path("/")
	            .maxAge(0)
	            .sameSite("None")
	            .build();

	    // Refresh Token 쿠키 만료
	    ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", "")
	            .httpOnly(true)
	            .secure(true)
	            .path("/")
	            .maxAge(0)
	            .sameSite("None")
	            .build();

	    response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
	    response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
	    
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
