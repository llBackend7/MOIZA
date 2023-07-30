package com.ll.MOIZA.boundedContext.calculater.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.member.repository.MemberRepository;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import com.ll.MOIZA.boundedContext.room.repository.EnterRoomRepository;
import com.ll.MOIZA.boundedContext.room.service.RoomService;
import com.ll.MOIZA.boundedContext.selectedTime.service.TimeRangeWithMember;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class DateTimeToMembersServiceThreadTest {

    @Autowired
    DateTimeToMembersService dateTimeToMembersService;
    @Autowired
    RoomService roomService;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    EnterRoomRepository enterRoomRepository;

    Room room;
    Member member1;
    Member member2;
    Member member3;

    @Test
    @DisplayName("동시성 테스트")
    @Transactional
    void multi_thread_test() {
        final int NUM_USERS = 3;

        final long ROOM_ID = room.getId();

        Member[] members = new Member[NUM_USERS + 1];

        for (int i = 1; i < NUM_USERS + 1; i++) {
            members[i] = memberRepository.findById(Long.valueOf(i)).orElse(new Member());
            System.out.println(members[i]);
        }

        for (int i = 0; i < 1; i++) {
            ExecutorService executorService = Executors.newFixedThreadPool(NUM_USERS);

            Runnable userTask = () -> {
                long userId = getAndIncrementUserId();
                dateTimeToMembersService.setDateTimeToMembers(ROOM_ID, LocalDate.now().plusDays(6),
                        LocalTime.of(0, 0, 0), members[(int) userId]);
                dateTimeToMembersService.setDateTimeToMembers(ROOM_ID, LocalDate.now().plusDays(6),
                        LocalTime.of(1, 0, 0), members[(int) userId]);
                dateTimeToMembersService.setDateTimeToMembers(ROOM_ID, LocalDate.now().plusDays(6),
                        LocalTime.of(2, 0, 0), members[(int) userId]);
                dateTimeToMembersService.setDateTimeToMembers(ROOM_ID, LocalDate.now().plusDays(6),
                        LocalTime.of(3, 0, 0), members[(int) userId]);
                dateTimeToMembersService.setDateTimeToMembers(ROOM_ID, LocalDate.now().plusDays(6),
                        LocalTime.of(4, 0, 0), members[(int) userId]);
                dateTimeToMembersService.setDateTimeToMembers(ROOM_ID, LocalDate.now().plusDays(6),
                        LocalTime.of(5, 0, 0), members[(int) userId]);
                dateTimeToMembersService.setDateTimeToMembers(ROOM_ID, LocalDate.now().plusDays(6),
                        LocalTime.of(6, 0, 0), members[(int) userId]);
                dateTimeToMembersService.setDateTimeToMembers(ROOM_ID, LocalDate.now().plusDays(6),
                        LocalTime.of(7, 0, 0), members[(int) userId]);
                dateTimeToMembersService.setDateTimeToMembers(ROOM_ID, LocalDate.now().plusDays(6),
                        LocalTime.of(8, 0, 0), members[(int) userId]);
                dateTimeToMembersService.setDateTimeToMembers(ROOM_ID, LocalDate.now().plusDays(6),
                        LocalTime.of(9, 0, 0), members[(int) userId]);
            };
            for (int j = 0; j < NUM_USERS; j++) {
                executorService.submit(userTask);
            }
            executorService.shutdown();

            try {
                executorService.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            List<TimeRangeWithMember> overlappingRanges = dateTimeToMembersService.getFindTOP10(
                    ROOM_ID, enterRoomRepository.findMembersByRoom(room));
            for (int j = 0; j < 10; j++) {
                TimeRangeWithMember tm = overlappingRanges.get(j);
                int finalJ = j;
                assertAll(
                        () -> assertThat(tm.getDate()).isEqualTo(LocalDate.now().plusDays(6)),
                        () -> assertThat(tm.getStart()).isEqualTo(LocalTime.of(finalJ, 0)),
                        () -> assertThat(tm.getEnd()).isEqualTo(LocalTime.of(finalJ + 3, 0)),
                        () -> assertThat(tm.getParticipationMembers()).isEqualTo(
                                List.of(member1, member2, member3))
                );
            }
        }
    }

    int userIdCounter = 0;

    private synchronized long getAndIncrementUserId() {
        return ++userIdCounter;
    }

    @BeforeEach
    void init() {
        member1 = memberRepository.findByName("user1").get();
        room = roomService.getRoom(3L);
        member2 = memberRepository.findByName("user2").get();
        member3 = memberRepository.findByName("user3").get();
    }
}
