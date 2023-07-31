package com.ll.MOIZA.boundedContext.calculater.service;

import com.ll.MOIZA.boundedContext.calculater.entity.DateTimeToMembers;
import com.ll.MOIZA.boundedContext.member.dto.MemberDTO;
import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import com.ll.MOIZA.boundedContext.room.service.RoomService;
import com.ll.MOIZA.boundedContext.selectedTime.entity.SelectedTime;
import com.ll.MOIZA.boundedContext.selectedTime.service.TimeRangeWithMember;
import com.ll.MOIZA.boundedContext.selectedTime.service.TimeRangeWithMemberDTO;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DateTimeToMembersService {

    static final int RANK_NUM = 10;
    private final RoomService roomService;
    private final Map<Long, DateTimeToMembers> dateTimeToMembersStorage = new ConcurrentHashMap<>();

    public DateTimeToMembers getDateTimeToMembers(Long roomId) {
        return dateTimeToMembersStorage.computeIfAbsent(roomId, key -> new DateTimeToMembers());
    }

    public void setDateTimeToMembers(Long roomId, LocalDate localDate, LocalTime localTime,
            Member member) {
        LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);

        DateTimeToMembers dateTimeToMembers = getDateTimeToMembers(roomId);
        dateTimeToMembers.setDateTimeToMembers(localDateTime, member);
    }

    public void deleteDateTimeToMembers(Long roomId, LocalDate localDate, LocalTime localTime,
            Member member) {
        DateTimeToMembers dateTimeToMembers = getDateTimeToMembers(roomId);
        LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);
        dateTimeToMembers.deleteDateTimeToMembers(localDateTime, member);
    }

    public void addDateTimeToMembers(Room room, SelectedTime selectedTime, Member member) {

        LocalDate date = selectedTime.getDate();
        LocalTime startTime = selectedTime.getStartTime();
        LocalTime endTime = selectedTime.getEndTime();
        LocalTime meetingDuration = room.getMeetingDuration();
        LocalTime curTime = startTime.plusHours(meetingDuration.getHour())
                .plusMinutes(meetingDuration.getMinute());

        while (!curTime.isAfter(endTime)) {
            setDateTimeToMembers(room.getId(), date, startTime, member);
            curTime = curTime.plusMinutes(30);
            startTime = startTime.plusMinutes(30);
        }
    }

    public List<TimeRangeWithMemberDTO> getFindTOP10(Long roomId, List<Member> allMembers) {
        DateTimeToMembers dateTimeToMembers = getDateTimeToMembers(roomId);
        Room room = roomService.getRoom(roomId);

        return dateTimeToMembers.getSortedEntries().stream()
                .limit(RANK_NUM)
                .map(entry -> {
                    List<MemberDTO> contain = getMembersDto(new ArrayList<>(entry.getValue()));
// todo
//                    List<MemberDTO> noContain = getNotContaion(allMembers, contain);

                    return new TimeRangeWithMemberDTO(
                            entry.getKey().toLocalDate(),
                            entry.getKey().toLocalTime(),
                            entry.getKey().toLocalTime()
                                    .plusHours(room.getMeetingDuration().getHour())
                                    .plusMinutes(room.getMeetingDuration().getMinute()),
                            contain,
                            new ArrayList<>()
                    );
                })
                .collect(Collectors.toList());
    }

    public List<TimeRangeWithMember> findOverlappingTimeRanges(Long roomId, List<Member> allMembers) {
        DateTimeToMembers dateTimeToMembers = getDateTimeToMembers(roomId);
        Room room = roomService.getRoom(roomId);

        return dateTimeToMembers.getSortedEntries().stream()
                .limit(RANK_NUM)
                .map(entry -> {
                    List<Member> contain = new ArrayList<>(entry.getValue());
                    List<Member> noContain = getNotContaion(allMembers, contain);

                    return new TimeRangeWithMember(
                            entry.getKey().toLocalDate(),
                            entry.getKey().toLocalTime(),
                            entry.getKey().toLocalTime()
                                    .plusHours(room.getMeetingDuration().getHour())
                                    .plusMinutes(room.getMeetingDuration().getMinute()),
                            contain,
                            noContain
                    );
                })
                .collect(Collectors.toList());
    }

    private List<Member> getNotContaion(List<Member> allMembers, List<Member> contain) {
        List<Member> noContain = new ArrayList<>(allMembers);
        contain.forEach(noContain::remove);
        return noContain;
    }

    private List<MemberDTO> getMembersDto(List<Member> members) {
        return members.stream()
                .map(member -> new MemberDTO(member.getId(), member.getName(),
                        member.getEmail(), member.getProfile()))
                .collect(Collectors.toList());
    }
}
