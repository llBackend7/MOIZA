package com.ll.MOIZA.boundedContext.selectedTime.service;

import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.member.repository.MemberRepository;
import com.ll.MOIZA.boundedContext.room.controller.RoomController;
import com.ll.MOIZA.boundedContext.room.entity.EnterRoom;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import com.ll.MOIZA.boundedContext.room.repository.EnterRoomRepository;
import com.ll.MOIZA.boundedContext.room.repository.RoomRepository;
import com.ll.MOIZA.boundedContext.room.service.EnterRoomService;
import com.ll.MOIZA.boundedContext.room.service.RoomService;
import com.ll.MOIZA.boundedContext.selectedTime.dto.SelectedTimeDto;
import com.ll.MOIZA.boundedContext.selectedTime.entity.SelectedTime;
import com.ll.MOIZA.boundedContext.selectedTime.repository.SelectedTimeRepository;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class SelectedTimeServiceTest {

    @Autowired
    SelectedTimeService selectedTimeService;

    @Autowired
    SelectedTimeRepository selectedTimeRepository;

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

    SelectedTime selectedTime;

    static Member member;
    static Member member2;
    static Room room;
    static EnterRoom enterRoom;
    static EnterRoom enterRoom2;

    @BeforeEach
    void init() {
        member = memberRepository.findByName("user1").get();
        member2 = memberRepository.findByName("user2").get();
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
        enterRoom = enterRoomService.createEnterRoom(room, member);
        enterRoom2 = enterRoomService.createEnterRoom(room, member2);

        selectedTime = selectedTimeService.CreateSelectedTime(
                LocalDate.now().plusDays(6),
                LocalTime.of(1, 0),
                LocalTime.of(4, 0),
                enterRoom
        );
    }

    @DisplayName("시간_선택_정상")
    @ParameterizedTest(name = "{index} - 기준 1~5시 시간:{0} 끝:{1}")
    @CsvSource({"1, 5", "1, 4", "2, 5"})
    void select_time_test(int st, int et) {

        SelectedTime selectedTime = selectedTimeService.CreateSelectedTime(
                LocalDate.now().plusDays(6),
                LocalTime.of(st, 0),
                LocalTime.of(et, 0),
                enterRoom
        );

        assertThat(selectedTime.getDate()).isEqualTo(LocalDate.now().plusDays(6));
    }

    @DisplayName("시간_선택_초과")
    @ParameterizedTest(name = "{index} - 기준 1~5시 시간:{0} 끝:{1}")
    @CsvSource({"0, 6", "0, 3", "3, 6", "1, 2", "3, 0"})
    void select_time_over_range_test(int st, int et) {

        assertThrows(ResponseStatusException.class, () -> {
            selectedTimeService.CreateSelectedTime(
                    LocalDate.now().plusDays(6),
                    LocalTime.of(st, 0),
                    LocalTime.of(et, 0),
                    enterRoom
            );
        });
    }

    @DisplayName("날짜_선택_정상")
    @ParameterizedTest(name = "{index} - 기준 5~7일, 입력 일:{0}")
    @CsvSource({"5", "6", "7"})
    void select_day_test(int day) {

        SelectedTime selectedTime = selectedTimeService.CreateSelectedTime(
                LocalDate.now().plusDays(day),
                LocalTime.of(1, 0),
                LocalTime.of(5, 0),
                enterRoom
        );

        assertThat(selectedTime.getDate()).isEqualTo(LocalDate.now().plusDays(day));
    }

    @DisplayName("날짜_선택_초과")
    @ParameterizedTest(name = "{index} - 기준 5~7일, 입력 일:{0}")
    @CsvSource({"4", "8"})
    void select_day_over_range_test(int day) {

        assertThrows(ResponseStatusException.class, () -> {
            selectedTimeService.CreateSelectedTime(
                    LocalDate.now().plusDays(day),
                    LocalTime.of(1, 0),
                    LocalTime.of(5, 0),
                    enterRoom
            );
        });
    }

    @Test
    @CsvSource({"0", "1", "2"})
    @DisplayName("timeRangeWithMember 비교 테스트")
    void timeRangeWithMember_compare_test() {
        TimeRangeWithMember basic = new TimeRangeWithMember(LocalDate.now(),
                LocalTime.of(0, 0), LocalTime.of(2, 0), List.of(new Member(), new Member()),
                List.of());
        TimeRangeWithMember sameBasic = new TimeRangeWithMember(LocalDate.now(),
                LocalTime.of(0, 0), LocalTime.of(2, 0), List.of(new Member(), new Member()),
                List.of());
        TimeRangeWithMember differentMemberSize = new TimeRangeWithMember(LocalDate.now(),
                LocalTime.of(0, 0), LocalTime.of(2, 0), List.of(new Member(), new Member(), new Member()),
                List.of());
        TimeRangeWithMember differentDay = new TimeRangeWithMember(LocalDate.now().plusDays(1),
                LocalTime.of(0, 0), LocalTime.of(2, 0), List.of(new Member(), new Member()),
                List.of());

        assertAll(
                // 같은 날, 같은 맴버수, 같은 시작 시간
                () -> assertThat(basic.compareTo(sameBasic)).isEqualTo(0),
                // 같은 날, 다른 맴버수, 같은 시작 시간
                () -> assertThat(basic.compareTo(differentMemberSize)).isEqualTo(1),
                // 다른 날, 같은 맴버수, 같은 시작 시간
                () -> assertThat(basic.compareTo(differentDay)).isEqualTo(-1)
        );

    }

    @Test
    @DisplayName("SelectedTime의 기간 구하기")
    void testGetDuration() {
        SelectedTime selectedTime = SelectedTime.builder()
                .endTime(LocalTime.of(2, 0))
                .startTime(LocalTime.of(0, 30))
                .build();

        assertThat(selectedTime.getDuration()).hasHour(1).hasMinute(30);
    }

    @Test
    @DisplayName("선택시간 변경")
    void changeTime() {
        RoomController.SelectedDayWithTime selectedDayWithTime = new RoomController.SelectedDayWithTime(
                LocalDate.now().plusDays(6),
                LocalTime.of(1, 0),
                LocalTime.of(4, 0));
        List<RoomController.SelectedDayWithTime> selectedDayWhitTimesList = List.of(selectedDayWithTime);
        enterRoom = enterRoomService.enterRoomWithSelectedTime(room,member,selectedDayWhitTimesList);

        assertThat(enterRoom.getSelectedTimes().stream()
                .map(time -> new RoomController.SelectedDayWithTime(time.getDate(), time.getStartTime(), time.getEndTime())))
                .containsExactly(selectedDayWithTime);
    }

    @Test
    @DisplayName("SelectedTime 추가하기")
    void testAddSelectedTime(){
        // given
        String start = selectedTime.getStartTime().toString();
        String end = selectedTime.getEndTime().toString();
        String day = selectedTime.getDate().toString();

        SelectedTimeDto dto = SelectedTimeDto.builder()
                .start(start)
                .end(end)
                .day(day)
                .build();

        // when
        SelectedTime savedSelectedTime = selectedTimeService.addSelectedTime(dto, enterRoom2);

        // then
        assertThat(savedSelectedTime.getStartTime().toString()).isEqualTo(start);
        assertThat(savedSelectedTime.getEndTime().toString()).isEqualTo(end);
        assertThat(savedSelectedTime.getDate().toString()).isEqualTo(day);
    }
}