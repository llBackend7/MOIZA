package com.ll.MOIZA.base.initData;

import com.ll.MOIZA.boundedContext.chat.service.ChatService;
import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.member.repository.MemberRepository;
import com.ll.MOIZA.boundedContext.result.entity.DecidedResult;
import com.ll.MOIZA.boundedContext.result.service.ResultService;
import com.ll.MOIZA.boundedContext.room.entity.EnterRoom;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import com.ll.MOIZA.boundedContext.room.service.EnterRoomService;
import com.ll.MOIZA.boundedContext.room.service.RoomService;
import com.ll.MOIZA.boundedContext.selectedPlace.service.SelectedPlaceService;
import com.ll.MOIZA.boundedContext.selectedTime.service.SelectedTimeService;
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

            Member member1 = Member.builder().name("user1").email("email").build();
            Member member2 = Member.builder().name("user2").email("email").build();
            try {
                memberRepository.save(member1);
            } catch (Exception e) {
            }
            try {
                memberRepository.save(member2);
            } catch (Exception e) {}

            Member member3 = Member.builder().name("user3").email("email").build();
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
                    LocalTime.of(13, 0),
                    enterRoom
            );

            selectedTimeService.CreateSelectedTime(
                    LocalDate.now().plusDays(6),
                    LocalTime.of(14, 0),
                    LocalTime.of(19, 0),
                    enterRoom
            );

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

            String place = "서울역";

            selectedPlaceService.CreateSelectedPlace(
                    place, enterRoom
            );
           resultService.createResult(place, enterRoom.getRoom());
        };
    }
}