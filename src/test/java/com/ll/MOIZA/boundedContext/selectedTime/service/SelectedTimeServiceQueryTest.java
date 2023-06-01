package com.ll.MOIZA.boundedContext.selectedTime.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.member.repository.MemberRepository;
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

    @Test
    void 조회_선택_날짜() {
        Room room = roomRepository.getReferenceById(1L);
        System.out.println(selectedTimeService.findSelectedTimeByEnterRoomAndDate(
                room, LocalDate.now().plusDays(6)).size());
    }

    @Test
    void 겹치는_시간_조회() {
        Member member1 = memberRepository.findByName("user1").get();
        Member member2 = memberRepository.findByName("user2").get();
        Member member3 = memberRepository.findByName("user3").get();
        Room room = roomRepository.getReferenceById(1L);

        List<SelectedTime> selectedTimeList = selectedTimeService.findSelectedTimeByEnterRoomAndDate(
                room,
                LocalDate.now().plusDays(6));

        List<TimeRangeWithMember> overlappingRanges = selectedTimeService.findOverlappingTimeRanges(
                selectedTimeList, room.getMeetingDuration().minusHours(1)
        );

        for (TimeRangeWithMember t : overlappingRanges) {
            System.out.println(t.date + " | " + t.start + "~" + t.end);
            for (Member m : t.getMembers()) {
                System.out.print(m.getName()+ " ");
            }
            System.out.println();
        }

        TimeRangeWithMember t1 = overlappingRanges.get(0);
        TimeRangeWithMember t2 = overlappingRanges.get(1);
        assertAll(
                () -> assertThat(t1.getDate()).isEqualTo(LocalDate.now().plusDays(6)),
                () -> assertThat(t1.getStart()).isEqualTo(LocalTime.of(8, 0)),
                () -> assertThat(t1.getEnd()).isEqualTo(LocalTime.of(10, 0)),
                () -> assertThat(t1.getMembers()).isEqualTo(List.of(member1, member2, member3)),

                () -> assertThat(t2.getDate()).isEqualTo(LocalDate.now().plusDays(6)),
                () -> assertThat(t2.getStart()).isEqualTo(LocalTime.of(11, 0)),
                () -> assertThat(t2.getEnd()).isEqualTo(LocalTime.of(13, 0)),
                () -> assertThat(t2.getMembers()).isEqualTo(List.of(member1, member2, member3))
        );
    }
}
