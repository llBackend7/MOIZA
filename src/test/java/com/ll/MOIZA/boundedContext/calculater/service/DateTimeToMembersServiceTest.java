package com.ll.MOIZA.boundedContext.calculater.service;

import com.ll.MOIZA.boundedContext.member.dto.MemberDTO;
import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.member.repository.MemberRepository;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import com.ll.MOIZA.boundedContext.room.repository.EnterRoomRepository;
import com.ll.MOIZA.boundedContext.room.service.RoomService;
import com.ll.MOIZA.boundedContext.selectedTime.service.TimeRangeWithMember;
import com.ll.MOIZA.boundedContext.selectedTime.service.TimeRangeWithMemberDTO;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@EnableCaching
@ActiveProfiles("test")
@Transactional
class DateTimeToMembersServiceTest {

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
    @DisplayName("삽입 및 삭제 테스트")
    @Transactional
    void RoomTreeMapServiceTest() {

        long roomId = room.getId();

        Map<LocalDateTime, Set<Member>> treeMap = dateTimeToMembersService.getDateTimeToMembers(
                        roomId)
                .getDateTimeToMembers();

        System.out.println(treeMap);

        dateTimeToMembersService.setDateTimeToMembers(roomId, LocalDate.now().plusDays(13),
                LocalTime.of(0, 0, 0),
                member1);
        dateTimeToMembersService.setDateTimeToMembers(roomId, LocalDate.now().plusDays(13),
                LocalTime.of(0, 0, 0),
                member2);
        dateTimeToMembersService.setDateTimeToMembers(roomId, LocalDate.now().plusDays(13),
                LocalTime.of(0, 0, 0),
                member3);
        dateTimeToMembersService.setDateTimeToMembers(roomId, LocalDate.now().plusDays(12),
                LocalTime.of(0, 0, 0),
                member2);
        dateTimeToMembersService.setDateTimeToMembers(roomId, LocalDate.now().plusDays(11),
                LocalTime.of(0, 0, 0),
                member3);

        System.out.println(treeMap);

        List<TimeRangeWithMemberDTO> overlappingRanges = dateTimeToMembersService.getFindTOP10(roomId,
                enterRoomRepository.findMembersByRoom(room));

        for (TimeRangeWithMemberDTO t : overlappingRanges) {
            System.out.println(t.getDate() + " | " + t.getStart() + "~" + t.getEnd());
            System.out.print("참가자 : ");
            for (MemberDTO m : t.getParticipationMembers()) {
                System.out.print(m.getName() + " ");
            }
            System.out.println();
            System.out.print("불참자 : ");
            for (MemberDTO m : t.getNonParticipationMembers()) {
                System.out.print(m.getName() + " ");
            }
            System.out.println();
        }

        dateTimeToMembersService.deleteDateTimeToMembers(roomId, LocalDate.now().plusDays(1),
                LocalTime.of(0, 0, 0), member3);

        List<TimeRangeWithMemberDTO> overlappingRanges2 = dateTimeToMembersService.getFindTOP10(roomId,
                enterRoomRepository.findMembersByRoom(room));

        for (TimeRangeWithMemberDTO t : overlappingRanges2) {
            System.out.println(t.getDate() + " | " + t.getStart() + "~" + t.getEnd());
            System.out.print("참가자 : ");
            for (MemberDTO m : t.getParticipationMembers()) {
                System.out.print(m.getName() + " ");
            }
            System.out.println();
            System.out.print("불참자 : ");
            for (MemberDTO m : t.getNonParticipationMembers()) {
                System.out.print(m.getName() + " ");
            }
            System.out.println();
        }
    }

    @BeforeEach
    void init() {
        member1 = memberRepository.findByName("user1").get();
        room = roomService.getRoom(2L);
        member2 = memberRepository.findByName("user2").get();
        member3 = memberRepository.findByName("user3").get();
    }

}
