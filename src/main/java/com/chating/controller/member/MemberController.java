package com.chating.controller.member;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chating.dto.SignInDTO;
import com.chating.dto.SignUpDTO;
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
	public ResponseEntity signUpUser(@RequestBody @Valid SignUpDTO userData) {
		memberService.signUpUser(userData);
		return ResponseEntity.ok("회원 가입 완료");
	}
	
	// 로그인 로직
	@PostMapping("singIn")
	public ResponseEntity signIn(@RequestBody @Valid SignInDTO userData)
	{
		memberService.signIn(userData);
		return ResponseEntity.ok("로그인이 완료되었습니다.");
	}
}
