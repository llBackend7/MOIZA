package com.ll.MOIZA.boundedContext.room.service;


import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.member.repository.MemberRepository;
import com.ll.MOIZA.boundedContext.room.entity.EnterRoom;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import com.ll.MOIZA.boundedContext.room.repository.EnterRoomRepository;
import com.ll.MOIZA.boundedContext.room.repository.RoomRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class EnterRoomServiceTest {

    @Autowired
    EnterRoomService enterRoomService;
    @Autowired
    RoomService roomService;

    @Autowired
    EnterRoomRepository enterRoomRepository;

    @Autowired
    RoomRepository roomRepository;

    @Autowired
    MemberRepository memberRepository;

    @Test
    void 빙_입장() {

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

        EnterRoom enterRoom = enterRoomService.createEnterRoom(
                room,
                member
        );

        assertAll(
                () -> assertThat(enterRoom.getMember().getName()).isEqualTo("user1"),
                () -> assertThat(enterRoom.getRoom().getName()).isEqualTo("테스트룸")
        );
    }
}
