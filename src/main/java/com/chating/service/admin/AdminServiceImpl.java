package com.chating.service.admin;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chating.common.CustomException;
import com.chating.dto.admin.MemberDetailDTO;
import com.chating.dto.admin.MemberListDTO;
import com.chating.dto.common.PageResponseDTO;
import com.chating.entity.member.Member;
import com.chating.entity.member.Status;
import com.chating.repository.member.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminServiceImpl implements AdminService {

    private final MemberRepository memberRepository;
    private final ModelMapper modelMapper;

    @Override
    public PageResponseDTO<MemberListDTO> getAllMembers(int pageCount, int size) {
        // 페이지 요청 생성 (최신순 정렬)
        Pageable pageable = PageRequest.of(pageCount, size, Sort.by("createdAt").descending());

        // 회원 조회
        Page<Member> memberPage = memberRepository.findAll(pageable);

        // Entity -> DTO 변환 (ModelMapper 사용)
        Page<MemberListDTO> dtoPage = memberPage.map(member ->
            modelMapper.map(member, MemberListDTO.class)
        );

        // PageResponseDTO로 변환 (생성자 사용)
        return new PageResponseDTO<>(dtoPage);
    }

    @Override
    public MemberDetailDTO getMemberDetail(String memberId) {
        // 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));

        // Entity -> DTO 변환 (ModelMapper 사용)
        return modelMapper.map(member, MemberDetailDTO.class);
    }

    @Override
    @Transactional
    public void updateStatus(String memberId, Status status) {
        // 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));

        // 상태 변경
        member.setStatus(status);

        // 저장 (더티 체킹으로 자동 업데이트)
        memberRepository.save(member);
    }

    @Override
    @Transactional
    public void deleteMember(String memberId) {
        // 회원 존재 확인
        if (!memberRepository.existsById(memberId)) {
            throw new CustomException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다.");
        }

        // 회원 삭제 (Cascade로 RefreshToken도 자동 삭제됨)
        memberRepository.deleteById(memberId);
    }
}