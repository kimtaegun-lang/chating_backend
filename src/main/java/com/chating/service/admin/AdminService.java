package com.chating.service.admin;

import com.chating.dto.admin.MemberDetailDTO;
import com.chating.dto.admin.MemberListDTO;
import com.chating.dto.common.PageResponseDTO;
import com.chating.dto.common.SearchOptionDTO;
import com.chating.entity.member.Status;
;

public interface AdminService {
    PageResponseDTO<MemberListDTO> getAllMembers(int pageCount, int size,SearchOptionDTO searchOptionDTO); // 회원 목록 조회
    MemberDetailDTO getMemberDetail(String memberId); // 상세 회원 조회
    void updateStatus(String memberId, Status status); // 회원 상태 업데이트
    void deleteMember(String memberId); // 회원 삭제
    void deleteRoom(Long roomId); // 회원 삭제
}