package com.ll.MOIZA.base.initData;

import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.member.repository.MemberRepository;
import com.ll.MOIZA.boundedContext.room.entity.EnterRoom;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import com.ll.MOIZA.boundedContext.room.service.EnterRoomService;
import com.ll.MOIZA.boundedContext.room.service.RoomService;
import com.ll.MOIZA.boundedContext.selectedTime.service.SelectedTimeService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Random;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"test"})
public class NotProd {
    @Bean
    CommandLineRunner commandLineRunner(
            MemberRepository memberRepository,
            RoomService roomService,
            EnterRoomService enterRoomService,
            SelectedTimeService selectedTimeService
    ) {
        return args -> {
            Member member1 = Member.builder().name("user1").password("1234").email("email").build();
            Member member2 = Member.builder().name("user2").password("1234").email("email").build();
            Member member3 = Member.builder().name("user3").password("1234").email("email").build();
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

            selectedTimeService.CreateSelectedTime(
                    LocalDate.now().plusDays(6),
                    LocalTime.of(7, 0),
                    LocalTime.of(11, 0),
                    enterRoom
            );

            selectedTimeService.CreateSelectedTime(
                    LocalDate.now().plusDays(6),
                    LocalTime.of(13, 0),
                    LocalTime.of(19, 0),
                    enterRoom
            );

            EnterRoom enterRoom2 = enterRoomService.createEnterRoom(room, member2);

            selectedTimeService.CreateSelectedTime(
                    LocalDate.now().plusDays(6),
                    LocalTime.of(6, 0),
                    LocalTime.of(10, 0),
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
                    LocalTime.of(12, 0),
                    enterRoom3
            );
        };
    }
}
