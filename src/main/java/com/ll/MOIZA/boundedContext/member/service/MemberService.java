package com.ll.MOIZA.boundedContext.member.service;

import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.member.repository.MemberRepository;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import com.ll.MOIZA.boundedContext.room.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final RoomRepository roomRepository;

    @Transactional
    public Member loginMember(User user) {
        Optional<Member> optionalMember = memberRepository.findByName(user.getUsername());
        if (optionalMember.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.");
        }
        return optionalMember.get();
    }

    public Member getMember(Long memberId) {
        return memberRepository
                .findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }

    public Member findByName(String name) {
        return memberRepository
                .findByName(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자[%s]를 찾을 수 없습니다.".formatted(name)));
    }

    @Transactional
    public Member whenSocialLogin(String nickname, String profile, String email) {
        Optional<Member> opMember = memberRepository.findByName(nickname);
        if(opMember.isPresent())
            return opMember.get();
        else
            return join(nickname, profile, email);
    }

    public Member join(String nickname, String profile, String email) {
        Member member = Member.builder()
                .name(nickname)
                .email(email)
                .profile(profile)
                .build();

        memberRepository.save(member);
        return member;
    }

    @Transactional
    public void deleteGroup(Long roomId) {
        Optional<Room> opRoom = roomRepository.findById(roomId);
        opRoom.ifPresent(roomRepository::delete);
    }
}
