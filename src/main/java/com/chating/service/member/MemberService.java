package com.chating.service.member;

import java.util.Map;

import com.chating.dto.member.MemberInfo;
import com.chating.dto.member.SignInDTO;
import com.chating.dto.member.SignUpDTO;
import com.chating.dto.member.UpdateMemberDTO;

public interface MemberService {
	public void signUpUser(SignUpDTO userData); // 회원 가입 로직
	public Map<String,String> signIn(SignInDTO userData); // 로그인 로직
	public void signOut(); // 로그아웃 로직
	public MemberInfo getMemberInfo(); // 회원 정보 얻는 로직
	 void updateMemberInfo(UpdateMemberDTO updateData);  // 회원 정보 수정
	 void deleteMember();  // 회원 탈퇴
}
