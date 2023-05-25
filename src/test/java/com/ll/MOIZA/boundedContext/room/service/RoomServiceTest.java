package com.ll.MOIZA.boundedContext.room.service;

import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.member.repository.MemberRepository;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class RoomServiceTest {
    @Autowired
    RoomService roomService;

    @Autowired
    MemberRepository memberRepository;

    @Test
    void 방_만들기() {
        Member member = memberRepository.findByName("user1").get();
        Room room = roomService.createRoom(
                member,
                "테스트룸",
                "테스트룸임",
                LocalDate.now(),
                LocalDate.now().plusDays(3),
                LocalTime.of(1, 0),
                LocalTime.of(5, 0),
                LocalTime.of(3, 0),
                LocalDateTime.now().plusDays(4));

        assertThat(room.getLeader().getId()).isEqualTo(member.getId());
        assertThat(room.getName()).isEqualTo("테스트룸");
    }
}