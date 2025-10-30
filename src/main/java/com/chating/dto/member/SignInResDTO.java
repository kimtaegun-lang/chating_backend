package com.chating.dto.member;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SignInResDTO {
    private String accessToken;
    private String refreshToken;
}
