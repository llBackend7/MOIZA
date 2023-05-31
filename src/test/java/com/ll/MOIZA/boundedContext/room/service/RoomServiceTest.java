package com.ll.MOIZA.boundedContext.room.service;

import com.ll.MOIZA.base.jwt.JwtProvider;
import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.member.repository.MemberRepository;
import com.ll.MOIZA.boundedContext.room.entity.EnterRoom;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class RoomServiceTest {
    @Autowired
    RoomService roomService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    JwtProvider jwtProvider;

    @Test
    void 방_만들기() {
        Member member = memberRepository.findByName("user1").get();
        Room room = roomService.createRoom(
                member,
                "테스트룸",
                "테스트룸임",
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(7),
                LocalTime.of(1, 0),
                LocalTime.of(5, 0),
                LocalTime.of(3, 0),
                LocalDateTime.now().plusDays(2));

        assertThat(room.getLeader().getId()).isEqualTo(member.getId());
        assertThat(room.getName()).isEqualTo("테스트룸");
    }

    @Test
    void 끝날짜_시작날짜보다_앞설경우_BAD_REQUEST() {
        assertThatThrownBy(() -> {
            Member member = memberRepository.findByName("user1").get();
            Room room = roomService.createRoom(
                    member,
                    "테스트룸",
                    "테스트룸임",
                    LocalDate.now(),
                    LocalDate.now().minusDays(3),
                    LocalTime.of(1, 0),
                    LocalTime.of(5, 0),
                    LocalTime.of(3, 0),
                    LocalDateTime.now().plusDays(4));
        })
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("가능날짜가 잘못되었습니다.");
    }

    @Test
    void 끝시간이_시작시간보다_앞설경우_BAD_REQUEST() {
        assertThatThrownBy(() -> {
            Member member = memberRepository.findByName("user1").get();
            Room room = roomService.createRoom(
                    member,
                    "테스트룸",
                    "테스트룸임",
                    LocalDate.now().plusDays(5),
                    LocalDate.now().plusDays(8),
                    LocalTime.of(14, 0),
                    LocalTime.of(10, 0),
                    LocalTime.of(3, 0),
                    LocalDateTime.now().plusDays(4));
        })
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("가능시간이 잘못되었습니다.");
    }

    @Test
    void 마감시간이_지금보다_앞설경우_BAD_REQUEST() {
        assertThatThrownBy(() -> {
            Member member = memberRepository.findByName("user1").get();
            Room room = roomService.createRoom(
                    member,
                    "테스트룸",
                    "테스트룸임",
                    LocalDate.now().plusDays(5),
                    LocalDate.now().plusDays(8),
                    LocalTime.of(10, 0),
                    LocalTime.of(14, 0),
                    LocalTime.of(3, 0),
                    LocalDateTime.now().minusDays(4));
        })
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("마감시간이 잘못되었습니다.");
    }

    @Test
    void 시작날짜_마감시간보다_앞설경우_BAD_REQUEST() {
        assertThatThrownBy(() -> {
            Member member = memberRepository.findByName("user1").get();
            Room room = roomService.createRoom(
                    member,
                    "테스트룸",
                    "테스트룸임",
                    LocalDate.now().plusDays(5),
                    LocalDate.now().plusDays(8),
                    LocalTime.of(10, 0),
                    LocalTime.of(14, 0),
                    LocalTime.of(3, 0),
                    LocalDateTime.now().plusDays(5));
        })
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("마감시간은 가능한 날짜보다 이전이어야 합니다.");
    }

    @Test
    void 액세스토큰_발급() {
        Member inviter = memberRepository.findByName("user1").get();

        Room room = roomService.createRoom(
                inviter,
                "테스트룸",
                "테스트룸임",
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(7),
                LocalTime.of(1, 0),
                LocalTime.of(5, 0),
                LocalTime.of(3, 0),
                LocalDateTime.now().plusDays(2));

        String accessToken = roomService.getAccessToken(room);
        assertThat(jwtProvider.getClaims(accessToken).get("accessCode")).isEqualTo(room.getAccessCode());
    }
}