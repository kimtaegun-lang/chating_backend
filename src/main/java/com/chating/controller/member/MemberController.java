package com.chating.controller.member;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chating.dto.member.SignInDTO;
import com.chating.dto.member.SignInResDTO;
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
	public ResponseEntity<SignInResDTO> signIn(@RequestBody @Valid SignInDTO userData)
	{
		 Map<String,String>tokens=memberService.signIn(userData);
		 SignInResDTO response = new SignInResDTO(tokens, "로그인 완료");
		 return ResponseEntity.ok(response);
	}
}
