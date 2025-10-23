package com.chating.dto.admin;


import com.chating.entity.member.Status;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatusDTO {
	private String memId;
	private Status status;
}
