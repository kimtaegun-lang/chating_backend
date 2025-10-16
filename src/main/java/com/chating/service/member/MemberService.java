package com.chating.service.member;

import com.chating.dto.SignInDTO;
import com.chating.dto.SignUpDTO;

public interface MemberService {
	public void signUpUser(SignUpDTO userData); // 회원 가입 로직
	public void signIn(SignInDTO userData); // 로그인 로직
}
