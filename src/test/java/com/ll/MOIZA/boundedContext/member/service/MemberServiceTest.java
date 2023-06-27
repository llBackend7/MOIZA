package com.ll.MOIZA.boundedContext.member.service;

import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.member.repository.MemberRepository;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import com.ll.MOIZA.boundedContext.room.repository.RoomRepository;
import com.ll.MOIZA.boundedContext.room.service.RoomService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.MethodName.class)
class MemberServiceTest {
    @Autowired
    private MemberService memberService;
    @Autowired
    private RoomService roomService;

    @Test
    @DisplayName("존재하는 이용자로 로그인하는 경우 해당 이용자의 정보를 불러옴")
    void loginMemberTest1() {
        // Given
        User user = new User("user1","", memberService.findByName("user1").getGrantedAuthorities());
        Member member = memberService.loginMember(user);

        // Then
        assertThat(member.getName()).isEqualTo("user1");
    }

    @Test
    @DisplayName("존재하지 않는 이용자로 로그인하는 경우 NOT FOUND 발생")
    void loginMemberTest2() {
        User user = new User("user100","", Collections.emptyList());

        assertThrows(ResponseStatusException.class, () -> {
            memberService.loginMember(user);
        });
    }

    @Test
    @DisplayName("memberId로 Member 정보 불러오기")
    void getMemberTest() {
        assertThat(memberService.getMember(1L).getName()).isEqualTo("user1");
        assertThrows(ResponseStatusException.class, () -> {
            memberService.getMember(100L);
        });
    }

    @Test
    @DisplayName("member 이름으로 Member 정보 불러오기")
    void findByNameTest() {
        assertThat(memberService.findByName("user1").getId()).isEqualTo(1L);
        assertThat(memberService.findByName("user2").getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("소셜로그인 - 새로운 멤버 로그인시 가입")
    void whenSocialLoginTest() {
        // Given
        String nickname = "username";
        String profile = "profile.jpg";
        String email = "user@example.com";

        // When
        Member member = memberService.whenSocialLogin(nickname, profile, email);

        // Then
        assertThat(member.getName()).isEqualTo(nickname);
        assertThat(member.getEmail()).isEqualTo(email);
        assertThat(member.getProfile()).isEqualTo(profile);
    }

    @Test
    @DisplayName("roomId로 해당 모임 삭제")
    void deleteGroupTest() {
        memberService.deleteGroup(1L);
        assertThrows(ResponseStatusException.class, () -> {
            roomService.getRoom(1L);
        });
    }
}