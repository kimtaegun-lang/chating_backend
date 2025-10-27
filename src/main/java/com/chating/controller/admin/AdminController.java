package com.chating.controller.admin;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chating.dto.admin.StatusDTO;
import com.chating.dto.common.SearchOptionDTO;
import com.chating.service.admin.AdminService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {
    
    private final AdminService adminService;
    
    // 전체 회원 조회 (페이징)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/members")
    public ResponseEntity<Map<String, Object>> getMembers(
            @RequestParam(value = "pageCount", defaultValue = "0") int pageCount,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @ModelAttribute(value="searchOption") SearchOptionDTO searchOptionDTO) {
        
    	System.out.println(searchOptionDTO);
        Map<String, Object> response = new HashMap<>();
        response.put("data", adminService.getAllMembers(pageCount, size,searchOptionDTO));
        response.put("message", "회원 목록 조회 완료");
        return ResponseEntity.ok(response);
    }

    // 회원 상세 조회
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/members/{memberId}")
    public ResponseEntity<Map<String, Object>> getMemberDetail(@PathVariable("memberId") String memberId) {
        Map<String, Object> response = new HashMap<>();
        response.put("data", adminService.getMemberDetail(memberId));
        response.put("message", "회원 상세 조회 완료");
        return ResponseEntity.ok(response);
    }

    // 회원 정지/해제
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/members/{memberId}/status")
    public ResponseEntity<String> updateMemberStatus(
            @PathVariable ("memberId") String memberId,
            @RequestBody StatusDTO status) {
        
        adminService.updateStatus(memberId, status.getStatus());
        return ResponseEntity.ok("회원 상태 변경 완료");
    }

    // 회원 삭제
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/members/{memberId}")
    public ResponseEntity<String> deleteMember(@PathVariable("memberId") String memberId) {
        adminService.deleteMember(memberId);
        return ResponseEntity.ok("회원 삭제 완료");
    }
    
    // 채팅방 삭제
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/members/deleteRoom/{roomId}")
    public ResponseEntity<String> deleteRoom(@PathVariable("roomId") Long roomId)
    {
    	adminService.deleteRoom(roomId);
    	return ResponseEntity.ok("채팅방 삭제 완료");
    }
}