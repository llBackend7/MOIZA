package com.ll.MOIZA.base.initData;

import com.ll.MOIZA.boundedContext.chat.service.ChatService;
import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.member.repository.MemberRepository;
import com.ll.MOIZA.boundedContext.result.service.ResultService;
import com.ll.MOIZA.boundedContext.room.entity.EnterRoom;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import com.ll.MOIZA.boundedContext.room.service.EnterRoomService;
import com.ll.MOIZA.boundedContext.room.service.RoomService;
import com.ll.MOIZA.boundedContext.selectedPlace.service.SelectedPlaceService;
import com.ll.MOIZA.boundedContext.selectedTime.service.SelectedTimeService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
@Profile({"test", "dev"})
public class NotProd {
    @Bean
    CommandLineRunner commandLineRunner(
            MemberRepository memberRepository,
            RoomService roomService,
            EnterRoomService enterRoomService,
            SelectedTimeService selectedTimeService,

            SelectedPlaceService selectedPlaceService,
            ResultService resultService,
            MongoTemplate mongoTemplate,
            ChatService chatService

    ) {
        return args -> {
            mongoTemplate.dropCollection("chat");

            Member member1 = Member.builder().name("user1").email("user1@email.com").profile("http://k.kakaocdn.net/dn/dpk9l1/btqmGhA2lKL/Oz0wDuJn1YV2DIn92f6DVK/img_640x640.jpg").build();
            Member member2 = Member.builder().name("user2").email("user2@email.com").profile("http://k.kakaocdn.net/dn/dpk9l1/btqmGhA2lKL/Oz0wDuJn1YV2DIn92f6DVK/img_640x640.jpg").build();
            Member member3 = Member.builder().name("이은혜").email("lutea67@naver.com").profile("http://k.kakaocdn.net/dn/dpk9l1/btqmGhA2lKL/Oz0wDuJn1YV2DIn92f6DVK/img_640x640.jpg").build();
            try {
                memberRepository.save(member1);
            } catch (Exception e) {}
            try {
                memberRepository.save(member2);
            } catch (Exception e) {}

            memberRepository.save(member1);
            memberRepository.save(member2);
            memberRepository.save(member3);

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
                    LocalTime.of(14, 0),
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
                    LocalTime.of(14, 0),
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
                    LocalTime.of(7, 0),
                    LocalTime.of(10, 0),
                    enterRoom3
            );

            selectedTimeService.CreateSelectedTime(
                    LocalDate.now().plusDays(6),
                    LocalTime.of(11, 0),
                    LocalTime.of(14, 0),
                    enterRoom3
            );

            Room room2 = roomService.createRoom(
                    member1,
                    "room2 test(마감기한 지남)",
                    "testtesttest",
                    LocalDate.now().plusDays(5),
                    LocalDate.now().plusDays(7),
                    LocalTime.of(0, 0),
                    LocalTime.of(23, 0),
                    LocalTime.of(3, 0),
                    LocalDateTime.now().plusSeconds(5));

            Room room3 = roomService.createRoom(
                    member3,
                    "room3 test(마감기한 안지남)",
                    "testtesttest",
                    LocalDate.now().plusDays(5),
                    LocalDate.now().plusDays(7),
                    LocalTime.of(0, 0),
                    LocalTime.of(23, 0),
                    LocalTime.of(3, 0),
                    LocalDateTime.now().plusDays(3));

            enterRoomService.createEnterRoom(room2, member1);
            enterRoomService.createEnterRoom(room2, member3);
            enterRoomService.createEnterRoom(room3, member3);

            // TODO: 시간 현황 페이지 만들어지고 나서 고쳐야함.
            resultService.createResult("강남역", room2);

            selectedPlaceService.CreateSelectedPlace("서울역", enterRoom3);
            selectedPlaceService.CreateSelectedPlace("대구역", enterRoom3);
            selectedPlaceService.CreateSelectedPlace("익산역", enterRoom3);

        };
    }
}