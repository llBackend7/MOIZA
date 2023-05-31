package com.ll.MOIZA.boundedContext.selectedTime.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.member.repository.MemberRepository;
import com.ll.MOIZA.boundedContext.room.entity.EnterRoom;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import com.ll.MOIZA.boundedContext.room.repository.EnterRoomRepository;
import com.ll.MOIZA.boundedContext.room.repository.RoomRepository;
import com.ll.MOIZA.boundedContext.room.service.EnterRoomService;
import com.ll.MOIZA.boundedContext.selectedTime.entity.SelectedTime;
import com.ll.MOIZA.boundedContext.selectedTime.repository.SelectedTimeRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class SelectedTimeServiceQueryTest {

    @Autowired
    SelectedTimeService selectedTimeService;

    @Autowired
    SelectedTimeRepository selectedTimeRepository;

    @Autowired
    EnterRoomService enterRoomService;

    @Autowired
    EnterRoomRepository enterRoomRepository;


    @Autowired
    MemberRepository memberRepository;

    @Autowired
    RoomRepository roomRepository;

    static EnterRoom enterRoom;

    @Test
    void 조회_선택_날짜() {
        Room room = roomRepository.getReferenceById(1L);
        System.out.println(selectedTimeService.findSelectedTimeByEnterRoomAndDate(
                room, LocalDate.now().plusDays(6)).size());
    }
}
