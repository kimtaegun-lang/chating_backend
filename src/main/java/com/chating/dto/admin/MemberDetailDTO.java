package com.chating.dto.admin;

import java.time.LocalDate;
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
public class MemberDetailDTO {
    private String memId;           // 아이디
    private String name;            // 이름
    private String email;           // 이메일
    private String phone;           // 핸드폰
    private Gender gender;          // 성별
    private String addr;            // 주소
    private LocalDate birth;        // 생년월일
    private Role role;              // 권한
    private LocalDateTime createdAt; // 가입일
}