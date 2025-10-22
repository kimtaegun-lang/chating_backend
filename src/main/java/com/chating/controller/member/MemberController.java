package com.chating.controller.member;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chating.dto.member.SignInDTO;
import com.chating.dto.member.SignUpDTO;
import com.chating.service.member.MemberService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("member/")
public class MemberController {
	private final MemberService memberService;
	
	// 회원 가입 로직
	@PostMapping("signUp")
	public ResponseEntity<String> signUpUser(@RequestBody @Valid SignUpDTO userData) {
		memberService.signUpUser(userData);
		return ResponseEntity.ok("회원 가입 완료");
	}
	
	// 로그인 로직
	@PostMapping("signIn")
	public ResponseEntity<Map<String,Object>> signIn(@RequestBody @Valid SignInDTO userData)
	{
		 Map<String,String>tokens=memberService.signIn(userData);
		 Map<String,Object>response=new HashMap<>();
		 response.put("data", tokens);
		 response.put("message", "로그인이 완료되었습니다.");
		
		 return ResponseEntity.ok(response);
	}
	
	// 로그아웃 로직
	@PreAuthorize("isAuthenticated()")
	@PostMapping("signOut")
	public ResponseEntity<String> signOut()
	{
		memberService.signOut();
		return ResponseEntity.ok("로그아웃이 완료 되었습니다.");
	}
	
	// 회원 정보 받아오는 로직
	@PreAuthorize("isAuthenticated()")
	@GetMapping("getMemberInfo")
	public ResponseEntity<Map<String,Object>> getMemberInfo()
	{
		Map<String,Object>response=new HashMap<>();
		response.put("data", memberService.getMemberInfo());
		response.put("message", "회원 정보 조회 완료");
		return ResponseEntity.ok(response);
	}
}
