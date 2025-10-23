package com.chating.dto.admin;

import java.time.LocalDateTime;

import com.chating.entity.member.Gender;
import com.chating.entity.member.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberListDTO {
    private String memId;           // 아이디
    private String name;            // 이름
    private String email;           // 이메일
    private Gender gender;          // 성별
    private Role role;              // 권한
    private LocalDateTime createdAt; // 가입일
}