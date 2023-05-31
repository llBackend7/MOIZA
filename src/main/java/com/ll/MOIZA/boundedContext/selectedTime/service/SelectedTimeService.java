package com.ll.MOIZA.boundedContext.selectedTime.service;

import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.room.entity.EnterRoom;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import com.ll.MOIZA.boundedContext.selectedTime.entity.SelectedTime;
import com.ll.MOIZA.boundedContext.selectedTime.repository.SelectedTimeRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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

    public List<SelectedTime> findSelectedTimeByEnterRoomAndDate(Room room, LocalDate date) {
        List<SelectedTime> selectedTimes = selectedTimeRepository.searchSelectedTimeByRoom(
                room, date);
        return selectedTimes;
    }

    public List<TimeRangeWithMember> findOverlappingTimeRanges(List<SelectedTime> selectedTimeList, LocalTime meetingDuration) {

        if (selectedTimeList.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "선택된 시간이 없습니다. 선택한 시간을 확인해 주세요.");
        }

        selectedTimeList.sort(Comparator.comparing(SelectedTime::getStartTime));

        LocalDate currentDate = selectedTimeList.get(0).getDate();
        LocalTime currentStart = selectedTimeList.get(0).getStartTime();
        LocalTime currentEnd = selectedTimeList.get(0).getEndTime();
        Member currentMember = selectedTimeList.get(0).getEnterRoom().getMember();

        List<TimeRangeWithMember> overlappingRanges = new ArrayList<>();

        List<Member> members = new ArrayList<>();
        members.add(currentMember);

        for (int i = 1; i < selectedTimeList.size(); i++) {
            TimeRangeWithMember currentRange = new TimeRangeWithMember(selectedTimeList.get(i).getDate(),
                    selectedTimeList.get(i).getStartTime(), selectedTimeList.get(i).getEndTime(),
                    members);

            Member hereMember = selectedTimeList.get(i).getEnterRoom().getMember();

            if (currentMember.equals(hereMember)) {
                continue;
            }
            if (!currentRange.start.plusHours(meetingDuration.getHour()).isAfter(currentEnd)) {
                if (currentRange.start.isAfter(currentStart)) {
                    currentStart = currentRange.start;
                }
                if (currentRange.end.isBefore(currentEnd)) {
                    currentEnd = currentRange.end;
                }
                currentRange.members.add(hereMember);
                currentRange.members.sort(Comparator.comparing(Member::getName));
            } else {
                //겹치지 않는 경우
                overlappingRanges.add(
                        new TimeRangeWithMember(currentDate, currentStart, currentEnd,
                                members));
                currentStart = currentRange.start;
                currentEnd = currentRange.end;
                currentMember = hereMember;
                // 기존 맴버 리스트 초기화 및 새로운 탐색 시작
                members = new ArrayList<>();
                members.add(currentMember);
            }
        }
        overlappingRanges.add(
                new TimeRangeWithMember(currentDate, currentStart, currentEnd,
                        members));

        Collections.sort(overlappingRanges);

        return overlappingRanges;
    }
}

@Getter
@AllArgsConstructor
class TimeRangeWithMember implements Comparable<TimeRangeWithMember>{

    LocalDate date;
    LocalTime start;
    LocalTime end;
    List<Member> members;

    @Override
    public int compareTo(TimeRangeWithMember o1) {
        if (o1.members.size() == members.size()) {
            return start.compareTo(o1.start);
        }
        return o1.members.size() - members.size();
    }
}