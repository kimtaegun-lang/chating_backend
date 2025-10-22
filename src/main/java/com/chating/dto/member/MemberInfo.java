package com.chating.dto.member;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MemberInfo {
	private String memId; // 아이디
	private String email; // 이메일
	private String name; // 이름
}
