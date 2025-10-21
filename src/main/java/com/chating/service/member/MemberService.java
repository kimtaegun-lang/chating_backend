package com.chating.service.member;

import java.util.Map;

import com.chating.dto.member.SignInDTO;
import com.chating.dto.member.SignUpDTO;

public interface MemberService {
	public void signUpUser(SignUpDTO userData); // 회원 가입 로직
	public Map<String,String> signIn(SignInDTO userData); // 로그인 로직
}
