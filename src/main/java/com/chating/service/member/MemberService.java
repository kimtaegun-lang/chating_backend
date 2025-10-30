package com.chating.service.member;

import com.chating.dto.member.MemberInfoDTO;
import com.chating.dto.member.SignInDTO;
import com.chating.dto.member.SignInResDTO;
import com.chating.dto.member.SignUpDTO;
import com.chating.dto.member.UpdateMemberDTO;

public interface MemberService {
	 void signUpUser(SignUpDTO userData); // 회원 가입 로직
	SignInResDTO signIn(SignInDTO userData); // 로그인 로직
	 void signOut(); // 로그아웃 로직
	 void updateMemberInfo(UpdateMemberDTO updateData);  // 회원 정보 수정
	 void deleteMember(String userId);  // 회원 탈퇴
	 MemberInfoDTO validateAndGetUserInfo(String accessToken); // accesstoken 검증 및 회원 정보 전달
}
