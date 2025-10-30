package com.chating.dto.member;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.chating.entity.member.Gender;
import com.chating.entity.member.Role;
import com.chating.entity.member.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberInfoDTO {
    private String memId;       // 아이디
    private String name;        // 이름
    private String email;       // 이메일
    private String phone;       // 전화번호
    private String addr;        // 주소
    private LocalDate birth;    // 생년월일
    private Gender gender;      // 성별
    private Role role;          // 권한
    private Status status;      // 상태
    private LocalDateTime createdAt;  // 가입일
}