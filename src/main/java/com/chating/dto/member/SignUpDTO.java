package com.chating.dto.member;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.chating.entity.member.Gender;
import com.chating.entity.member.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SignUpDTO {

	@NotBlank(message = "아이디를 입력해 주세요.")
	@Size(min = 6, max = 12, message = "아이디는 6자 이상 12자 이내로 입력해주세요.")
	@Pattern(regexp = "^[A-Za-z0-9]+$", message = "아이디는 영문과 숫자만 사용할 수 있습니다.")
	private String memId; // 아이디

	@NotBlank(message = "비밀번호를 입력해 주세요.")
	@Size(min = 6, max = 12, message = "비밀번호는 6자 이상 12자 이내로 입력해주세요.")
	@Pattern(regexp = "^(?=.*[!@#$%^&*])[A-Za-z0-9!@#$%^&*]+$", message = "비밀번호는 영문, 숫자, 특수문자만 가능하며 특수문자를 최소 1개 포함해야 합니다.")
	private String pwd; // 비밀번호

	@NotBlank(message = "이름을 입력해 주세요.")
	private String name; // 이름

	@NotBlank(message = "이메일을 입력해 주세요.")
	@Email(message = "올바른 이메일 형식으로 입력해 주세요.")
	private String email; // 이메일

	@NotBlank(message = "전화번호를 입력해 주세요.")
	@Pattern(regexp = "^01[016789]-?\\d{3,4}-?\\d{4}$", message = "올바른 전화번호 형식으로 입력해 주세요.")
	private String phone; // 핸드폰 번호

	@NotNull(message = "성별을 선택해 주세요.")
	private Gender gender; // 성별

	@NotBlank(message = "주소를 입력해 주세요.")
	private String addr; // 주소

	private LocalDateTime createdAt; // 생성 일자

	@NotNull(message = "생년월일을 입력해 주세요.")
	private LocalDate birth; // 생년월일

	private Role role; // 권한
}
