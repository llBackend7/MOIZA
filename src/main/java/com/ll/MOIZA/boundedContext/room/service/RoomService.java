package com.ll.MOIZA.boundedContext.room.service;

import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import com.ll.MOIZA.boundedContext.room.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;

    @Transactional
    public Room createRoom(Member member,
                           String name,
                           String description,
                           LocalDate startDay,
                           LocalDate endDay,
                           LocalTime availableStartTime,
                           LocalTime availableEndTime,
                           LocalTime duration,
                           LocalDateTime deadLine) {
        validateTimes(startDay, endDay, availableStartTime, availableEndTime, deadLine);

        Room room = Room.builder()
                .leader(member)
                .name(name)
                .description(description)
                .availableStartDay(startDay)
                .availableEndDay(endDay)
                .availableStartTime(availableStartTime)
                .availableEndTime(availableEndTime)
                .meetingDuration(duration)
                .deadLine(deadLine)
                .accessCode(UUID.randomUUID().toString())
                .build();
        Room createdRoom = roomRepository.save(room);

        return createdRoom;
    }

    private void validateTimes(LocalDate startDay, LocalDate endDay, LocalTime availableStartTime, LocalTime availableEndTime, LocalDateTime deadLine) {
        if (!startDay.isBefore(endDay)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "가능날짜가 잘못되었습니다.");
        }
        if (!availableStartTime.isBefore(availableEndTime)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "가능시간이 잘못되었습니다.");
        }
        if (!deadLine.isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "마감시간이 잘못되었습니다.");
        }
        if (!deadLine.isBefore(startDay.atStartOfDay())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "마감시간은 가능한 날짜보다 이전이어야 합니다.");
        }
    }

    public void invite(Room room, Member member) {

    }
}
