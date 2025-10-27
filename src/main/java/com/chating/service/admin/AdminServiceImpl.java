package com.chating.service.admin;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chating.common.CustomException;
import com.chating.dto.admin.MemberDetailDTO;
import com.chating.dto.admin.MemberListDTO;
import com.chating.dto.common.PageResponseDTO;
import com.chating.dto.common.SearchOptionDTO;
import com.chating.entity.member.Member;
import com.chating.entity.member.Status;
import com.chating.repository.chat.ChatRoomRepository;
import com.chating.repository.member.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminServiceImpl implements AdminService {

    private final MemberRepository memberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ModelMapper modelMapper;

    // 회원 정보 조회
    @Override
    public PageResponseDTO<MemberListDTO> getAllMembers(
            int pageCount, 
            int size, 
            SearchOptionDTO searchOptionDTO) {
        
        // Pageable 생성 
        Pageable pageable = PageRequest.of(pageCount, size);
        
        // 검색 쿼리 사용
        Page<Member> memberPage = memberRepository.findAllWithSearch(
                searchOptionDTO.getSearchType(),
                searchOptionDTO.getSearch(),
                searchOptionDTO.getSortType(),
                pageable
        );
        
        // Entity -> DTO 변환
        Page<MemberListDTO> dtoPage = memberPage.map(member ->
                modelMapper.map(member, MemberListDTO.class)
        );
        
        return new PageResponseDTO<>(dtoPage);
    }

    // 회원 상세정보 조회
    @Override
    public MemberDetailDTO getMemberDetail(String memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));

        return modelMapper.map(member, MemberDetailDTO.class);
    }

    // 회원 상태 변경
    @Override
    @Transactional
    public void updateStatus(String memberId, Status status) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));

        member.setStatus(status);
        memberRepository.save(member);
    }

    // 회원 탈퇴
    @Override
    @Transactional
    public void deleteMember(String memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new CustomException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다.");
        }

        memberRepository.deleteById(memberId);
    }
    
    @Override
    @Transactional
    public void deleteRoom(Long roomId) {
    	System.out.println(roomId);
    	chatRoomRepository.deleteById(roomId);
    }
}