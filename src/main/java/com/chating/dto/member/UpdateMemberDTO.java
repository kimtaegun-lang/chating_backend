package com.chating.dto.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMemberDTO {
    
    @Email(message = "올바른 이메일 형식으로 입력해 주세요.")
    private String email;
    
    @Pattern(regexp = "^01[016789]-?\\d{3,4}-?\\d{4}$", message = "올바른 전화번호 형식으로 입력해 주세요.")
    private String phone;
    
    private String addr;
    
    // 비밀번호 변경 시 현재 비밀번호 확인용
    private String currentPwd;
    
    // 새 비밀번호 (선택사항)
    @Size(min = 6, max = 12, message = "비밀번호는 6자 이상 12자 이내로 입력해주세요.")
    @Pattern(regexp = "^(?=.*[!@#$%^&*])[A-Za-z0-9!@#$%^&*]+$", message = "비밀번호는 영문, 숫자, 특수문자만 가능하며 특수문자를 최소 1개 포함해야 합니다.")
    private String newPwd;
}