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
        
        return User.builder()
            .username(member.getMemId()) // 아이디
            .password(member.getPwd())  // 암호화된 비밀번호
            .authorities(getAuthorities(member))  // 권한 설정
            .build();
    }
    
    // 권한 설정
    private Collection<? extends GrantedAuthority> getAuthorities(Member member) {
        return Collections.singleton( // 한 사용자당 권한을 한 개씩 밖게 못 가지므로 singleton 사용
            new SimpleGrantedAuthority("ROLE_" + member.getRole().name())); // .name은 enum타입을 문자로 변환
    }
}
