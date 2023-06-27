package com.ll.MOIZA.boundedContext.room.scheduler;

import com.ll.MOIZA.base.mail.MailService;
import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.result.entity.DecidedResult;
import com.ll.MOIZA.boundedContext.result.repository.ResultRepository;
import com.ll.MOIZA.boundedContext.result.service.ResultService;
import com.ll.MOIZA.boundedContext.room.entity.EnterRoom;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import com.ll.MOIZA.boundedContext.room.repository.RoomRepository;
import com.ll.MOIZA.boundedContext.room.scheduler.Scheduler;
import com.ll.MOIZA.boundedContext.selectedPlace.entity.SelectedPlace;
import com.ll.MOIZA.boundedContext.selectedPlace.service.SelectedPlaceService;
import com.ll.MOIZA.boundedContext.selectedTime.service.SelectedTimeService;
import com.ll.MOIZA.boundedContext.selectedTime.service.TimeRangeWithMember;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class SchedulerTest {
    List<Room> roomToMailSend;

    @BeforeEach
    void prepareDataSet() throws NoSuchFieldException, IllegalAccessException {
        Member member1 = Member.builder().id(11L).build();
        Member member2 = Member.builder().id(12L).build();
        Member member3 = Member.builder().id(13L).build();
        Member member4 = Member.builder().id(14L).build();
        Member member5 = Member.builder().id(15L).build();

        Room room1 = Room.builder().id(1L).build();
        Room room2 = Room.builder().id(2L).build();
        Room room3 = Room.builder().id(3L).build();
        Room room4 = Room.builder().id(4L).build();
        Room room5 = Room.builder().id(5L).build();

        /*
            room1: [member1, member2, member3]
            room2: [member4]
            room3: [member5]
            room4: [member1, member2]
            room5: [member3]
         */

        List<EnterRoom> enterRooms1 = List.of(
                EnterRoom.builder().room(room1).member(member1).build(),
                EnterRoom.builder().room(room1).member(member2).build(),
                EnterRoom.builder().room(room1).member(member3).build());

        List<EnterRoom> enterRooms2 = List.of(
                EnterRoom.builder().room(room2).member(member4).build());

        List<EnterRoom> enterRooms3 = List.of(
                EnterRoom.builder().room(room3).member(member5).build());

        List<EnterRoom> enterRooms4 = List.of(
                EnterRoom.builder().room(room4).member(member1).build(),
                EnterRoom.builder().room(room4).member(member2).build());

        List<EnterRoom> enterRooms5 = List.of(
                EnterRoom.builder().room(room5).member(member3).build());

        Field enterRoom = Room.class.getDeclaredField("enterRoom");
        enterRoom.setAccessible(true);

        enterRoom.set(room1, enterRooms1);
        enterRoom.set(room2, enterRooms2);
        enterRoom.set(room3, enterRooms3);
        enterRoom.set(room4, enterRooms4);
        enterRoom.set(room5, enterRooms5);

        roomToMailSend = List.of(room1, room2, room3, room4, room5);
    }

    @Test
    void testCheckConfirmedRoomAndSendMail() {
        RoomRepository roomRepository = Mockito.mock(RoomRepository.class);
        MailService mailService = Mockito.mock(MailService.class);
        SpringTemplateEngine templateEngine = Mockito.mock(SpringTemplateEngine.class);
        SelectedTimeService selectedTimeService = Mockito.mock(SelectedTimeService.class);
        SelectedPlaceService selectedPlaceService = Mockito.mock(SelectedPlaceService.class);
        ResultRepository resultRepository = Mockito.mock(ResultRepository.class);

        when(roomRepository.findByDeadLineBeforeAndMailSentFalse(Mockito.any(LocalDateTime.class)))
                .thenReturn(roomToMailSend);

        when(templateEngine.process(Mockito.anyString(), Mockito.any()))
                .thenReturn("메일내용");

        Scheduler scheduler = new Scheduler(roomRepository, mailService, templateEngine, selectedTimeService, selectedPlaceService, resultRepository);

        scheduler.checkConfirmedRoomAndSendMail();

        // 테스트데이터셋의 마감된 방의 모든 인원수(중복포함)의 합만큼 메일이 보내져야함
        verify(mailService, times(8)).sendMailTo(Mockito.any(Member.class), Mockito.anyString());

        // 마감된 방 수만큼 repository가 save호출
        verify(roomRepository, times(roomToMailSend.size())).save(Mockito.any(Room.class));

        assertThat(roomToMailSend.stream().map(Room::isMailSent)).allMatch(mailSent -> mailSent);
    }

    @Test
    void testCheckConfirmedRoomAndCreateResult() {
        // Mock dependencies
        RoomRepository roomRepository = Mockito.mock(RoomRepository.class);
        ResultRepository resultRepository = Mockito.mock(ResultRepository.class);
        SelectedTimeService selectedTimeService = Mockito.mock(SelectedTimeService.class);
        SelectedPlaceService selectedPlaceService = Mockito.mock(SelectedPlaceService.class);
        MailService mailService = Mockito.mock(MailService.class);
        SpringTemplateEngine templateEngine = Mockito.mock(SpringTemplateEngine.class);

        // Create sample data
        Room room = Room.builder().id(1L).build();

        TimeRangeWithMember timeRange = TimeRangeWithMember.builder()
                .date(LocalDate.now())
                .start(LocalTime.now())
                .participationMembers(Collections.singletonList(new Member()))
                .nonParticipationMembers(Collections.emptyList())
                .build();

        List<TimeRangeWithMember> overlappingTimes = Collections.singletonList(timeRange);

        Map<SelectedPlace, Long> selectedPlaces = Collections.singletonMap(new SelectedPlace(), 1L);

        // Configure mock behavior
        when(roomRepository.findByDeadLineBefore(Mockito.any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(room));

        when(resultRepository.findByRoomId(anyLong()))
                .thenReturn(Optional.empty());

        when(selectedTimeService.findOverlappingTimeRanges(Mockito.any(Room.class)))
                .thenReturn(overlappingTimes);

        when(selectedPlaceService.getSelectedPlaces(Mockito.any(Room.class)))
                .thenReturn(selectedPlaces);

        // Call the method under test
        Scheduler scheduler = new Scheduler(roomRepository, mailService, templateEngine, selectedTimeService, selectedPlaceService, resultRepository);
        scheduler.checkConfirmedRoomAndCreateResult();

        // Verify the behavior
        verify(resultRepository, times(1)).save(Mockito.any(DecidedResult.class));
    }
}