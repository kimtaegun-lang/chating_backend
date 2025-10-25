package com.chating.dto.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchOptionDTO {
	private String search;
	private String searchType;
	private String sort;
	private String sortType;
}
