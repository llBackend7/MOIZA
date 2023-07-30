package com.ll.MOIZA.boundedContext.selectedTime.service;

import com.ll.MOIZA.boundedContext.calculater.service.DateTimeToMembersService;
import com.ll.MOIZA.boundedContext.room.entity.EnterRoom;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import com.ll.MOIZA.boundedContext.room.repository.EnterRoomRepository;
import com.ll.MOIZA.boundedContext.selectedTime.dto.SelectedTimeDto;
import com.ll.MOIZA.boundedContext.selectedTime.entity.SelectedTime;
import com.ll.MOIZA.boundedContext.selectedTime.repository.SelectedTimeRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SelectedTimeService {

    private final SelectedTimeRepository selectedTimeRepository;
    private final DateTimeToMembersService dateTimeToMembersService;
    private final EnterRoomRepository enterRoomRepository;
    @Value("${custom.site.calculatorUrl}")
    private String calculatorUrl;

    @Transactional
    public SelectedTime CreateSelectedTime(LocalDate day,
            LocalTime startTime,
            LocalTime endTime,
            EnterRoom enterRoom) {
        validDate(enterRoom.getRoom(), day);
        validTime(enterRoom.getRoom(), startTime, endTime);
        SelectedTime selectedTime = SelectedTime.builder()
                .date(day)
                .startTime(startTime)
                .endTime(endTime)
                .enterRoom(enterRoom)
                .build();
        dateTimeToMembersService.addDateTimeToMembers(enterRoom.getRoom(), selectedTime,
                enterRoom.getMember());

        enterRoom.getSelectedTimes().add(selectedTime);
        return selectedTimeRepository.save(selectedTime);
    }

    @Transactional
    public SelectedTime addSelectedTime(SelectedTimeDto selectedTimeDto, EnterRoom enterRoom) {
        Integer[] dayArray = Arrays.stream(selectedTimeDto.getDay().split("-"))
                .map(Integer::parseInt).toArray(Integer[]::new);
        Integer[] startArray = Arrays.stream(selectedTimeDto.getStart().split(":"))
                .map(Integer::parseInt).toArray(Integer[]::new);
        Integer[] endArray = Arrays.stream(selectedTimeDto.getEnd().split(":"))
                .map(Integer::parseInt).toArray(Integer[]::new);

        LocalDate day = LocalDate.of(dayArray[0], dayArray[1], dayArray[2]);
        LocalTime start = LocalTime.of(startArray[0], startArray[1]);
        LocalTime end = LocalTime.of(endArray[0], endArray[1]);
        SelectedTime selectedTime = CreateSelectedTime(day, start, end, enterRoom);
        dateTimeToMembersService.setDateTimeToMembers(enterRoom.getRoom().getId(), day, start,
                enterRoom.getMember());
        return selectedTime;
    }

    private void validDate(Room room, LocalDate day) {
        if (room.getAvailableStartDay().isAfter(day)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "선택할 수 있는 날짜가 아닙니다. 선택한 날짜가 가능한 날짜보다 이릅니다.");
        }
        if (room.getAvailableEndDay().isBefore(day)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "선택할 수 있는 날짜가 아닙니다. 선택한 날짜가 가능한 날짜보다 늦습니다.");
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
        if (endTime.minusHours(startTime.getHour()).minusMinutes(startTime.getMinute())
                .isBefore(room.getMeetingDuration())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "선택할 수 있는 시간이 아닙니다. 미팅 진행 시간보다 짧은 시간입니다.");
        }
    }

    public List<TimeRangeWithMember> findOverlappingTimeRanges(Room room) {
        return dateTimeToMembersService.getFindTOP10(room.getId(),
                enterRoomRepository.findMembersByRoom(room));
    }
}