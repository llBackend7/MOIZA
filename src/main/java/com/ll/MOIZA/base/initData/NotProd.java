package com.ll.MOIZA.base.initData;

import com.ll.MOIZA.boundedContext.chat.service.ChatService;
import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.member.repository.MemberRepository;
import com.ll.MOIZA.boundedContext.room.entity.EnterRoom;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import com.ll.MOIZA.boundedContext.room.service.EnterRoomService;
import com.ll.MOIZA.boundedContext.room.service.RoomService;
import com.ll.MOIZA.boundedContext.selectedTime.service.SelectedTimeService;
import com.ll.MOIZA.boundedContext.selectedPlace.service.SelectedPlaceService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Configuration
@Profile({"test", "dev"})
public class NotProd {

    @Value("${custom.user.name}")
    private String name;

    @Bean
    CommandLineRunner commandLineRunner(
            MemberRepository memberRepository,
            RoomService roomService,
            EnterRoomService enterRoomService,
            SelectedTimeService selectedTimeService,

            SelectedPlaceService selectedPlaceService,

            MongoTemplate mongoTemplate,
            ChatService chatService
    ) {
        return args -> {
            mongoTemplate.dropCollection("chat");
            Member member1 = Member.builder()
                    .name(name)
                    .build();
            Member member2 = Member.builder().name("user2").email("email").build();
            Member member3 = Member.builder().name("user3").email("email").build();
            Member member4 = Member.builder().name("user4").email("email").build();

            try {
                memberRepository.save(member1);
            } catch (Exception e) {
            }
            try {
                memberRepository.save(member2);
            } catch (Exception e) {}


            memberRepository.save(member1);
            memberRepository.save(member2);
            memberRepository.save(member3);
            memberRepository.save(member4);

            Room room = roomService.createRoom(
                    member1,
                    "테스트룸",
                    "테스트룸임",
                    LocalDate.now().plusDays(5),
                    LocalDate.now().plusDays(7),
                    LocalTime.of(0, 0),
                    LocalTime.of(23, 0),
                    LocalTime.of(3, 0),
                    LocalDateTime.now().plusDays(2));
            EnterRoom enterRoom = enterRoomService.createEnterRoom(room, member1);

            for (int i = 0; i < 100; i++) {
                chatService.sendChat(member1, room, "테스트 채팅%d".formatted(i));
            }

            selectedTimeService.CreateSelectedTime(
                    LocalDate.now().plusDays(6),
                    LocalTime.of(7, 0),
                    LocalTime.of(13, 0),
                    enterRoom
            );

            selectedTimeService.CreateSelectedTime(
                    LocalDate.now().plusDays(6),
                    LocalTime.of(14, 0),
                    LocalTime.of(19, 0),
                    enterRoom
            );

            selectedPlaceService.CreateSelectedPlace("서울역", enterRoom);
            selectedPlaceService.CreateSelectedPlace("용산역", enterRoom);

            EnterRoom enterRoom2 = enterRoomService.createEnterRoom(room, member2);

            selectedTimeService.CreateSelectedTime(
                    LocalDate.now().plusDays(6),
                    LocalTime.of(6, 0),
                    LocalTime.of(13, 0),
                    enterRoom2
            );

            selectedTimeService.CreateSelectedTime(
                    LocalDate.now().plusDays(6),
                    LocalTime.of(15, 0),
                    LocalTime.of(17, 0),
                    enterRoom2
            );

            selectedPlaceService.CreateSelectedPlace("서울역", enterRoom2);
            selectedPlaceService.CreateSelectedPlace("용산역", enterRoom2);
            selectedPlaceService.CreateSelectedPlace("사당역", enterRoom2);

            EnterRoom enterRoom3 = enterRoomService.createEnterRoom(room, member3);

            selectedTimeService.CreateSelectedTime(
                    LocalDate.now().plusDays(6),
                    LocalTime.of(8, 0),
                    LocalTime.of(10, 0),
                    enterRoom3
            );

            selectedTimeService.CreateSelectedTime(
                    LocalDate.now().plusDays(6),
                    LocalTime.of(11, 0),
                    LocalTime.of(13, 0),
                    enterRoom3
            );

            selectedPlaceService.CreateSelectedPlace("서울역", enterRoom3);
            selectedPlaceService.CreateSelectedPlace("대구역", enterRoom3);
            selectedPlaceService.CreateSelectedPlace("익산역", enterRoom3);

            EnterRoom enterRoom4 = enterRoomService.createEnterRoom(room, member4);

            selectedTimeService.CreateSelectedTime(
                    LocalDate.now().plusDays(6),
                    LocalTime.of(9, 0),
                    LocalTime.of(10, 0),
                    enterRoom4
            );

            selectedTimeService.CreateSelectedTime(
                    LocalDate.now().plusDays(6),
                    LocalTime.of(11, 0),
                    LocalTime.of(16, 0),
                    enterRoom4
            );

            selectedPlaceService.CreateSelectedPlace("신도림역", enterRoom4);
            selectedPlaceService.CreateSelectedPlace("부산역", enterRoom4);
            selectedPlaceService.CreateSelectedPlace("오송역", enterRoom4);
        };
    }
}