package com.ll.MOIZA.boundedContext.member.service;

import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.member.repository.MemberRepository;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import com.ll.MOIZA.boundedContext.room.repository.RoomRepository;
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
    private MemberRepository memberRepository;
    @Autowired
    private RoomRepository roomRepository;

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
        // Given
        User user = new User("user2","", memberService.findByName("user2").getGrantedAuthorities());
        Member member = memberService.findByName("user2");
        memberRepository.delete(member);

        // When
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            memberService.loginMember(user);
        });

        // Then
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getReason()).isEqualTo("사용자를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("memberId로 Member 정보 불러오기")
    void getMemberTest() {
       assertThat(memberService.getMember(1L).getName()).isEqualTo("user1");

        when(memberService.getMember(3L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
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
    void testDeleteGroup_ExistingRoom() {
        Member member = memberService.getMember(1L);
        int roomCnt = member.getRooms().size();
        memberService.deleteGroup(1L);

        assertThat(member.getRooms().size()).isEqualTo(roomCnt-1);
    }
}