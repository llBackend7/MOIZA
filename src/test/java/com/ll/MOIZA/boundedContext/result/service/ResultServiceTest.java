package com.ll.MOIZA.boundedContext.result.service;

import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.member.service.MemberService;
import com.ll.MOIZA.boundedContext.result.repository.ResultRepository;
import com.ll.MOIZA.boundedContext.result.service.ResultService;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import com.ll.MOIZA.boundedContext.room.service.EnterRoomService;
import com.ll.MOIZA.boundedContext.room.service.RoomService;
import com.ll.MOIZA.boundedContext.selectedTime.service.TimeRangeWithMember;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.MethodName.class)
class ResultServiceTest {

    @Autowired
    private MemberService memberService;
    @Autowired
    private RoomService roomService;
    @Autowired
    private EnterRoomService enterRoomService;
    @Autowired
    private ResultService resultService;
    @Autowired
    private ResultRepository resultRepository;

    private Room room;
    private TimeRangeWithMember timeRangeWithMember;

    @BeforeEach
    void init() {
        Member member1 = memberService.getMember(1L);
        Member member2 = memberService.getMember(2L);
        Member member3 = memberService.getMember(3L);

        // Given
        room = roomService.createRoom(
                member2,
                "test",
                "test desc",
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(7),
                LocalTime.of(0, 0),
                LocalTime.of(23, 0),
                LocalTime.of(3, 0),
                LocalDateTime.now().plusSeconds(5));

        enterRoomService.createEnterRoom(room, member1);
        enterRoomService.createEnterRoom(room, member3);

        LocalDate date = LocalDate.now();
        LocalTime start = LocalTime.of(10, 0);
        LocalTime end = LocalTime.of(12, 0);
        List<Member> participationMembers = Arrays.asList(member1, member3);
        List<Member> nonParticipationMembers = List.of(member2);
        timeRangeWithMember = new TimeRangeWithMember(date, start, end, participationMembers, nonParticipationMembers);
    }

    @Test
    @DisplayName("결과 데이터 만들기 테스트")
    void createResultTest() {
        resultService.createResult(room, timeRangeWithMember, "testPlace");
        assertThat(resultRepository.findByRoomId(room.getId()).get().getDecidedPlace()).isEqualTo("testPlace");
    }

    @Test
    @DisplayName("결과 데이터 불러오기 테스트 - 존재하지 않는 결과 불러올 시 오류 발생")
    void getResultTest() {
        assertThat(resultService.getResult(2L).getId()).isEqualTo(resultRepository.findByRoomId(2L).get().getId());
        assertThrows(ResponseStatusException.class, () -> {
            resultService.getResult(100L);
        });
    }
}
