package com.chating.dto.common;
import java.util.List;

import org.springframework.data.domain.Page;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@ToString
@AllArgsConstructor
@Data
public class PageResponseDTO <T>{
	 private List<T> content;
	    private int totalPages;
	   private int currentPage;
	   private Long totalElements;
	    public PageResponseDTO(Page<T> page) {
	        this.content = page.getContent(); // 페이지 요소
	        this.totalPages = page.getTotalPages(); // 전체 페이지
	        this.currentPage = page.getNumber(); // 현제 페이지
	        this.totalElements=page.getTotalElements(); // 전체 요소 갯수
	    }
	    
	 
}
