package com.chating.security;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.chating.entity.member.Member;
import com.chating.repository.member.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String memId) throws UsernameNotFoundException {
        Member member = memberRepository.findByMemId(memId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        // Security에서 사용하는 UserDetails 객체로 변환하여 반환
        return User.builder()
                .username(member.getMemId())      // 로그인 아이디
                .password(member.getPwd())        // 암호화된 비밀번호
                .authorities(getAuthorities(member)) // 권한(ROLE)
                .build();
    }


    private Collection<? extends GrantedAuthority> getAuthorities(Member member) {
        return Collections.singleton(
                new SimpleGrantedAuthority("ROLE_" + member.getRole().name())
        );
    }
}
