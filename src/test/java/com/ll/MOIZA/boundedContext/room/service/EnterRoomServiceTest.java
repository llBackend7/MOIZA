package com.ll.MOIZA.boundedContext.room.service;


import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.member.repository.MemberRepository;
import com.ll.MOIZA.boundedContext.room.controller.RoomController;
import com.ll.MOIZA.boundedContext.room.controller.RoomController.SelectedDayWithTime;
import com.ll.MOIZA.boundedContext.room.entity.EnterRoom;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import com.ll.MOIZA.boundedContext.room.repository.EnterRoomRepository;
import com.ll.MOIZA.boundedContext.room.repository.RoomRepository;
import com.ll.MOIZA.boundedContext.room.service.EnterRoomService;
import com.ll.MOIZA.boundedContext.room.service.RoomService;
import com.ll.MOIZA.boundedContext.selectedTime.entity.SelectedTime;
import com.ll.MOIZA.boundedContext.selectedTime.service.SelectedTimeService;
import java.util.List;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

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

    @Autowired
    SelectedTimeService selectedTimeService;


    static EnterRoom enterRoom;
    static Member member;
    static Room room;
    @BeforeEach
    void beforeEach() {
        member = memberRepository.findByName("user1").get();
        room = roomService.createRoom(
                member,
                "테스트룸",
                "테스트룸임",
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(7),
                LocalTime.of(1, 0),
                LocalTime.of(5, 0),
                LocalTime.of(3, 0),
                LocalDateTime.now().plusDays(2));

        enterRoom = enterRoomService.createEnterRoom(
                room,
                member
        );
    }


    @Test
    void 방_입장() {
        assertAll(
                () -> assertThat(enterRoom.getMember().getName()).isEqualTo("user1"),
                () -> assertThat(enterRoom.getRoom().getName()).isEqualTo("테스트룸")
        );
    }

    @Test
    void removeOverlappingTimeTest() {

        List<RoomController.SelectedDayWithTime> selectedDayWithTimeList = List.of(
                new SelectedDayWithTime(
                        LocalDate.now().plusDays(5),
                        LocalTime.of(2, 0),
                        LocalTime.of(5, 0)),
                new SelectedDayWithTime(
                        LocalDate.now().plusDays(5),
                        LocalTime.of(2, 0),
                        LocalTime.of(5, 0)),
                new SelectedDayWithTime(
                        LocalDate.now().plusDays(5),
                        LocalTime.of(3, 0),
                        LocalTime.of(4, 0)),
                new SelectedDayWithTime(
                        LocalDate.now().plusDays(5),
                        LocalTime.of(1, 0),
                        LocalTime.of(4, 0)),
                new SelectedDayWithTime(
                        LocalDate.now().plusDays(5),
                        LocalTime.of(5, 0),
                        LocalTime.of(7, 0)),
                new SelectedDayWithTime(
                        LocalDate.now().plusDays(5),
                        LocalTime.of(8, 0),
                        LocalTime.of(9, 0))
        );

        System.out.println(enterRoomService.removeOverlappingTime(selectedDayWithTimeList));

    }
}
