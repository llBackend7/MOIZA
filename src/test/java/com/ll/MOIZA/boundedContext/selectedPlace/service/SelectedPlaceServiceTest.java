package com.ll.MOIZA.boundedContext.selectedPlace.service;

import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.member.repository.MemberRepository;
import com.ll.MOIZA.boundedContext.room.entity.EnterRoom;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import com.ll.MOIZA.boundedContext.room.repository.EnterRoomRepository;
import com.ll.MOIZA.boundedContext.room.repository.RoomRepository;
import com.ll.MOIZA.boundedContext.room.service.EnterRoomService;
import com.ll.MOIZA.boundedContext.room.service.RoomService;
import com.ll.MOIZA.boundedContext.selectedPlace.entity.SelectedPlace;
import com.ll.MOIZA.boundedContext.selectedPlace.repository.SelectedPlaceRepository;
import java.util.Map.Entry;
import javax.swing.text.html.parser.Entity;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class SelectedPlaceServiceTest {

    @Autowired
    SelectedPlaceRepository selectedPlaceRepository;

    @Autowired
    SelectedPlaceService selectedPlaceService;

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

    static Member member;
    static Room room;
    static EnterRoom enterRoom;
    @BeforeEach
    void before_test() {
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

        enterRoom = enterRoomService.createEnterRoom(room, member);
    }

    @Test
    @DisplayName("장소_선택")
    void select_place_test() {

        SelectedPlace selectedPlace = selectedPlaceService.CreateSelectedPlace("테스트 장소", enterRoom);

        assertAll(
                () -> assertThat(selectedPlace.getName()).isEqualTo("테스트 장소"),
                () -> assertThat(selectedPlace.getEnterRoom().getId()).isEqualTo(enterRoom.getId()),
                () -> assertThrows(ResponseStatusException.class, () -> {
                    selectedPlaceService.CreateSelectedPlace("테스트 장소", enterRoom);
                })
        );
    }

    @Test
    @DisplayName("장소_선택")
    void select_place() {
        room = roomService.getRoom(2L);

        Map<SelectedPlace, Long> selectedPlaces = selectedPlaceService.getSelectedPlaces(room);

        Assertions.assertThat(selectedPlaces.size()).isEqualTo(1);
    }
}