package com.ll.MOIZA.boundedContext.room.service;

import com.ll.MOIZA.base.initData.NotProd;
import com.ll.MOIZA.base.jwt.JwtProvider;
import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.member.repository.MemberRepository;
import com.ll.MOIZA.boundedContext.result.repository.ResultRepository;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import com.ll.MOIZA.boundedContext.room.service.RoomService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@Import(NotProd.class)
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
    void 끝시간이_시작시간과_같아도_BAD_REQUEST() {
        assertThatThrownBy(() -> {
            Member member = memberRepository.findByName("user1").get();
            Room room = roomService.createRoom(
                    member,
                    "테스트룸",
                    "테스트룸임",
                    LocalDate.now().plusDays(5),
                    LocalDate.now().plusDays(8),
                    LocalTime.of(10, 0),
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
                    LocalTime.of(0, 0),
                    LocalTime.of(14, 0),
                    LocalTime.of(3, 0),
                    LocalDateTime.now().plusDays(5));
        })
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("마감시간은 가능한 날짜보다 이전이어야 합니다.");
    }

    @Test
    void 가능시작시간이_30분단위가_아니면_BAD_REQUEST() {
        assertThatThrownBy(() -> {
            Member member = memberRepository.findByName("user1").get();
            Room room = roomService.createRoom(
                    member,
                    "테스트룸",
                    "테스트룸임",
                    LocalDate.now().plusDays(5),
                    LocalDate.now().plusDays(8),
                    LocalTime.of(10, 12),
                    LocalTime.of(14, 0),
                    LocalTime.of(3, 0),
                    LocalDateTime.now().plusDays(4));
        })
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("시간은 30분 단위로 입력 가능합니다.");
    }

    @Test
    void 가능끝시간이_30분단위가_아니면_BAD_REQUEST() {
        assertThatThrownBy(() -> {
            Member member = memberRepository.findByName("user1").get();
            Room room = roomService.createRoom(
                    member,
                    "테스트룸",
                    "테스트룸임",
                    LocalDate.now().plusDays(5),
                    LocalDate.now().plusDays(8),
                    LocalTime.of(10, 30),
                    LocalTime.of(14, 24),
                    LocalTime.of(3, 0),
                    LocalDateTime.now().plusDays(4));
        })
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("시간은 30분 단위로 입력 가능합니다.");
    }

    @Test
    void 기간이_30분단위가_아니면_BAD_REQUEST() {
        assertThatThrownBy(() -> {
            Member member = memberRepository.findByName("user1").get();
            Room room = roomService.createRoom(
                    member,
                    "테스트룸",
                    "테스트룸임",
                    LocalDate.now().plusDays(5),
                    LocalDate.now().plusDays(8),
                    LocalTime.of(10, 30),
                    LocalTime.of(14, 0),
                    LocalTime.of(3, 23),
                    LocalDateTime.now().plusDays(4));
        })
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("시간은 30분 단위로 입력 가능합니다.");
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

    @Test
    @DisplayName("모임 마감 테스트")
    void closeRoomTest() {
        roomService.closeRoom(1L);
        Room room = roomService.getRoom(1L);
        assertThat(room.getDeadLine().getDayOfMonth()).isEqualTo(LocalDateTime.now().getDayOfMonth());
    }
}