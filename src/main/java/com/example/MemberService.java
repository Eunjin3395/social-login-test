package com.example;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public Member login(String email, String nickname) {
        // kakao로부터 받아온 사용자 정보(email, nickname)을 가지고 우리 db에 해당 회원이 존재하면 로그인, 존재하지 않으면 회원가입 시키기

        Optional<Member> member = memberRepository.findMemberByEmail(email);
        if (member.isPresent()) { // 해당 정보를 갖는 회원이 존재하면, 로그인

            return member.get();

        }else{ // 해당 정보를 갖는 회원이 존재하지 않으면, 회원가입
            Member newMember = Member.builder()
                    .email(email)
                    .name(nickname)
                    .build();

            memberRepository.save(newMember);
            return newMember;
        }
    }
}
