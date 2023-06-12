package com.ll.MOIZA.boundedContext.room.scheduler;

import com.ll.MOIZA.base.mail.MailService;
import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.result.repository.ResultRepository;
import com.ll.MOIZA.boundedContext.result.service.ResultService;
import com.ll.MOIZA.boundedContext.room.entity.EnterRoom;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import com.ll.MOIZA.boundedContext.room.repository.RoomRepository;
import com.ll.MOIZA.boundedContext.selectedPlace.service.SelectedPlaceService;
import com.ll.MOIZA.boundedContext.selectedTime.service.SelectedTimeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
    void testCheckConfirmedRoom() {
        RoomRepository roomRepository = Mockito.mock(RoomRepository.class);
        MailService mailService = Mockito.mock(MailService.class);
        SpringTemplateEngine templateEngine = Mockito.mock(SpringTemplateEngine.class);
        SelectedTimeService selectedTimeService = Mockito.mock(SelectedTimeService.class);
        SelectedPlaceService selectedPlaceService = Mockito.mock(SelectedPlaceService.class);
        ResultRepository resultRepository = Mockito.mock(ResultRepository.class);

        Mockito.when(roomRepository.findByDeadLineBeforeAndMailSentFalse(Mockito.any(LocalDateTime.class)))
                .thenReturn(roomToMailSend);

        Mockito.when(templateEngine.process(Mockito.anyString(), Mockito.any()))
                .thenReturn("메일내용");

        Scheduler scheduler = new Scheduler(roomRepository, mailService, templateEngine, selectedTimeService, selectedPlaceService, resultRepository);

        scheduler.checkConfirmedRoom();

        // 테스트데이터셋의 마감된 방의 모든 인원수(중복포함)의 합만큼 메일이 보내져야함
        Mockito.verify(mailService, Mockito.times(8)).sendMailTo(Mockito.any(Member.class), Mockito.anyString());

        // 마감된 방 수만큼 repository가 save호출
        Mockito.verify(roomRepository, Mockito.times(roomToMailSend.size())).save(Mockito.any(Room.class));

        assertThat(roomToMailSend.stream().map(Room::isMailSent)).allMatch(mailSent -> mailSent);
    }
}