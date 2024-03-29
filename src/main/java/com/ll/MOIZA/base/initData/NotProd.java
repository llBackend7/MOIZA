package com.ll.MOIZA.base.initData;

import com.ll.MOIZA.boundedContext.chat.document.Chat;
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

import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.ll.MOIZA.boundedContext.selectedTime.service.TimeRangeWithMember;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

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
            RedisTemplate<String, Chat> redisTemplate,
            ChatService chatService

    ) {
        return args -> {
            mongoTemplate.dropCollection("chat");
            try{
                redisTemplate.delete("ROOM#1_CHAT");
            }catch (RedisConnectionFailureException e){}


            Member member1 = Member.builder().name("user1").email("user1@email.com").profile("http://k.kakaocdn.net/dn/dpk9l1/btqmGhA2lKL/Oz0wDuJn1YV2DIn92f6DVK/img_640x640.jpg").build();
            Member member2 = Member.builder().name("user2").email("user2@email.com").profile("http://k.kakaocdn.net/dn/dpk9l1/btqmGhA2lKL/Oz0wDuJn1YV2DIn92f6DVK/img_640x640.jpg").build();
            Member member3 = Member.builder().name("user3").email("lutea67@naver.com").profile("http://k.kakaocdn.net/dn/dpk9l1/btqmGhA2lKL/Oz0wDuJn1YV2DIn92f6DVK/img_640x640.jpg").build();
            Member member4 = Member.builder().name("정상화").email("mouse4786@gmail.com").profile("http://k.kakaocdn.net/dn/dpk9l1/btqmGhA2lKL/Oz0wDuJn1YV2DIn92f6DVK/img_640x640.jpg").build();

            try {
                memberRepository.save(member1);
            } catch (Exception e) {
            }
            try {
                memberRepository.save(member2);
            } catch (Exception e) {
            }
            try {
                memberRepository.save(member3);
            } catch (Exception e) {
            }
            try {
                memberRepository.save(member4);
            } catch (Exception e) {
            }

            Room room = roomService.createRoom(
                    member1,
                    "테스트1",
                    "테스트룸임",
                    LocalDate.now().plusDays(5),
                    LocalDate.now().plusDays(7),
                    LocalTime.of(0, 0),
                    LocalTime.of(23, 0),
                    LocalTime.of(3, 0),
                    LocalDateTime.now().plusDays(2));

            EnterRoom enterRoom = enterRoomService.createEnterRoom(room, member1);

            for (int i = 0; i < 78; i++) {
                Chat chat = Chat.builder()
                        .roomId("1")
                        .writer("user1")
                        .memberId("1")
                        .content("테스트 채팅 몽고db#%d".formatted(i))
                        .build();
                mongoTemplate.save(chat);
            }

            try{
                ZSetOperations<String, Chat> ops = redisTemplate.opsForZSet();
                for (int i = 0; i < 100; i++) {
                    Chat chat = Chat.builder()
                            .roomId("1")
                            .writer("user1")
                            .memberId("1")
                            .content("테스트 채팅 레디스db#%d".formatted(i))
                            .createDate(LocalDateTime.now())
                            .modifyDate(LocalDateTime.now())
                            .build();
                    ops.add("ROOM#1_CHAT", chat, -chat.getCreateDate().toInstant(ZoneOffset.UTC).toEpochMilli());
                }
            }catch (RedisConnectionFailureException e){}


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

            selectedTimeService.CreateSelectedTime(
                    LocalDate.now().plusDays(5),
                    LocalTime.of(7, 0),
                    LocalTime.of(14, 0),
                    enterRoom
            );

            selectedTimeService.CreateSelectedTime(
                    LocalDate.now().plusDays(5),
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
                    LocalTime.of(18, 0),
                    enterRoom2
            );

            selectedTimeService.CreateSelectedTime(
                    LocalDate.now().plusDays(5),
                    LocalTime.of(6, 0),
                    LocalTime.of(14, 0),
                    enterRoom2
            );

            selectedTimeService.CreateSelectedTime(
                    LocalDate.now().plusDays(5),
                    LocalTime.of(15, 0),
                    LocalTime.of(18, 0),
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

            selectedTimeService.CreateSelectedTime(
                    LocalDate.now().plusDays(5),
                    LocalTime.of(7, 0),
                    LocalTime.of(10, 0),
                    enterRoom3
            );

            selectedTimeService.CreateSelectedTime(
                    LocalDate.now().plusDays(5),
                    LocalTime.of(11, 0),
                    LocalTime.of(14, 0),
                    enterRoom3
            );

            Optional<EnterRoom> opEnterRoom = enterRoomService.findByMemberIdAndRoomId(1L, 1L);
            Optional<EnterRoom> opEnterRoom2 = enterRoomService.findByMemberIdAndRoomId(2L, 1L);

            Room room2 = roomService.createRoom(
                    member1,
                    "테스트2 (마감기한 지남)",
                    "testtesttest",
                    LocalDate.now().plusDays(5),
                    LocalDate.now().plusDays(7),
                    LocalTime.of(0, 0),
                    LocalTime.of(23, 0),
                    LocalTime.of(3, 0),
                    LocalDateTime.now().plusSeconds(5));

            Room room3 = roomService.createRoom(
                    member3,
                    "테스트3 (마감기한 안지남)",
                    "testtesttest",
                    LocalDate.now().plusDays(5),
                    LocalDate.now().plusDays(7),
                    LocalTime.of(0, 0),
                    LocalTime.of(23, 0),
                    LocalTime.of(3, 0),
                    LocalDateTime.now().plusDays(3));

            EnterRoom enterRoom4 = enterRoomService.createEnterRoom(room2, member1);
            EnterRoom enterRoom5 = enterRoomService.createEnterRoom(room2, member3);
            enterRoomService.createEnterRoom(room2, member4);
            enterRoomService.createEnterRoom(room3, member4);

            selectedPlaceService.CreateSelectedPlace("서울역", enterRoom3);
            selectedPlaceService.CreateSelectedPlace("대구역", enterRoom3);
            selectedPlaceService.CreateSelectedPlace("익산역", enterRoom3);

            selectedPlaceService.CreateSelectedPlace("강남역", enterRoom4);

            selectedTimeService.CreateSelectedTime(
                    LocalDate.now().plusDays(5),
                    LocalTime.of(12, 0),
                    LocalTime.of(18, 0),
                    enterRoom4
            );
            selectedTimeService.CreateSelectedTime(
                    LocalDate.now().plusDays(5),
                    LocalTime.of(15, 0),
                    LocalTime.of(19, 0),
                    enterRoom5
            );

            LocalDate date = LocalDate.of(2023, 6, 12);
            LocalTime start = LocalTime.of(10, 0);
            LocalTime end = LocalTime.of(12, 0);
            List<Member> participationMembers = Arrays.asList(member1, member3);
            List<Member> nonParticipationMembers = List.of(member2);
            TimeRangeWithMember timeRangeWithMember = new TimeRangeWithMember(date, start, end, participationMembers, nonParticipationMembers);
            resultService.createResult(room2, timeRangeWithMember, "강남역");

            EnterRoom enterRoom6 = enterRoomService.createEnterRoom(room3, member3);
            EnterRoom enterRoom7 = enterRoomService.createEnterRoom(room3, member1);
            EnterRoom enterRoom8 = enterRoomService.createEnterRoom(room3, member3);
        };
    }
}