package com.ll.MOIZA.boundedContext.selectedTime.service;

import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.room.entity.EnterRoom;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import com.ll.MOIZA.boundedContext.room.service.RoomService;
import com.ll.MOIZA.boundedContext.selectedTime.entity.SelectedTime;
import com.ll.MOIZA.boundedContext.selectedTime.repository.SelectedTimeRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SelectedTimeService {

    private final SelectedTimeRepository selectedTimeRepository;

    private final RoomService roomService;

    @Transactional
    public SelectedTime CreateSelectedTime(
            LocalDate day,
            LocalTime startTime,
            LocalTime endTime,
            EnterRoom enterRoom
    ) {
        validDate(enterRoom.getRoom(), day);
        validTime(enterRoom.getRoom(), startTime, endTime);
        SelectedTime selectedTime = SelectedTime.builder()
                .date(day)
                .startTime(startTime)
                .endTime(endTime)
                .enterRoom(enterRoom)
                .build();

        return selectedTimeRepository.save(selectedTime);
    }

    private void validDate(Room room, LocalDate day) {
        if (room.getAvailableStartDay().isAfter(day)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "선택할 수 있는 날짜이 아닙니다. 선택한 날짜이 가능한 날짜보다 이릅니다.");
        }
        if (room.getAvailableEndDay().isBefore(day)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "선택할 수 있는 날짜이 아닙니다. 선택한 날짜이 가능한 날짜보다 늦습니다.");
        }
    }

    private void validTime(Room room, LocalTime startTime, LocalTime endTime) {
        if (startTime.isAfter(endTime)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "시작하는 시간은 끝나는 시간보다 빠를 수 없습니다.");
        }
        if (room.getAvailableStartTime().isAfter(startTime)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "선택할 수 있는 시간이 아닙니다. 선택한 시간이 가능한 시간보다 이릅니다.");
        }
        if (room.getAvailableEndTime().isBefore(endTime)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "선택할 수 있는 시간이 아닙니다. 선택한 시간이 가능한 시간보다 늦습니다.");
        }
    }

    public List<TimeRangeWithMember> findOverlappingTimeRanges(Room room) {
        List<TimeRangeWithMember> timeRangeWithMembers = new LinkedList<>();

        LocalDate startDay = room.getAvailableStartDay();
        LocalDate endDay = room.getAvailableEndDay();

        while (!startDay.isAfter(endDay)) {
            List<TimeRangeWithMember> getTimeRangesWhitRoomAndDay = findOverlappingTimeRanges(room,
                    startDay);

            timeRangeWithMembers.addAll(getTimeRangesWhitRoomAndDay);

            startDay = startDay.plusDays(1);
        }

        Collections.sort(timeRangeWithMembers);

        if (timeRangeWithMembers.size() > 10) {
            return new ArrayList<>(timeRangeWithMembers.subList(0, 10));
        }

        return timeRangeWithMembers;
    }

    public List<TimeRangeWithMember>findOverlappingTimeRanges(
            Room room, LocalDate date) {

        List<SelectedTime> selectedTimeList = selectedTimeRepository.searchSelectedTimeByRoom(room,
                date);

        if (selectedTimeList.isEmpty()) {
            return new ArrayList<>();
        }

        List<TimeRangeWithMember> overlappingRanges = new LinkedList<>();

        // 탐색 시간 기준 시작점
        LocalTime startTime = room.getAvailableStartTime();

        LocalTime meetingDuration = room.getMeetingDuration();

        while (startTime.isBefore(room.getAvailableEndTime())) {

            LocalTime basicStartTime = startTime;
            LocalTime basicEndTime = startTime.plusHours(meetingDuration.getHour())
                    .plusMinutes(meetingDuration.getMinute());

            List<Member> members = getContainedMember(selectedTimeList, meetingDuration,
                    basicStartTime,
                    basicEndTime);

            if (members.size() > 1) {
                overlappingRanges.add(
                        new TimeRangeWithMember(date, basicStartTime, basicEndTime, members));
            }

            startTime = basicStartTime.plusMinutes(30);
        }

        Collections.sort(overlappingRanges);

        if (overlappingRanges.size() > 10) {
            return new ArrayList<>(overlappingRanges.subList(0, 10));
        }

        return overlappingRanges;
    }

    private List<Member> getContainedMember(List<SelectedTime> selectedTimeList,
            LocalTime meetingDuration, LocalTime startTime, LocalTime endTime) {
        return selectedTimeList.stream()
                .filter(selectedTime ->
                        !selectedTime.getEndTime()
                                .minusHours(selectedTime.getStartTime().getHour())
                                .minusMinutes(selectedTime.getStartTime().getMinute())
                                .isBefore(meetingDuration)
                )
                .filter(selectedTime -> !selectedTime.getStartTime().isAfter(endTime))
                .filter(selectedTime ->
                        !selectedTime.getStartTime().isAfter(startTime) &&
                                !selectedTime.getEndTime().isBefore(endTime)
                )
                .map(SelectedTime::getEnterRoom)
                .map(EnterRoom::getMember)
                .sorted(Comparator.comparing(Member::getName))
                .collect(Collectors.toCollection(LinkedList::new));
    }


    @Getter
    @AllArgsConstructor
    public class TimeRangeWithMember implements Comparable<TimeRangeWithMember> {

        LocalDate date;
        LocalTime start;
        LocalTime end;
        List<Member> members;

        @Override
        public int compareTo(TimeRangeWithMember o1) {
            if (o1.members.size() == members.size()) {
                if (o1.date.isEqual(date)) {
                    return start.compareTo(o1.start);
                }
                return date.compareTo(o1.date);
            }
            return o1.members.size() - members.size();
        }
    }

}