package com.ll.MOIZA.boundedContext.room.service;

import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import com.ll.MOIZA.boundedContext.room.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
