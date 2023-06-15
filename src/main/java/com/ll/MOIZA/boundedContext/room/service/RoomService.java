package com.ll.MOIZA.boundedContext.room.service;

import com.ll.MOIZA.base.jwt.JwtProvider;
import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.room.entity.EnterRoom;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import com.ll.MOIZA.boundedContext.room.repository.RoomRepository;

import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;

    private final JwtProvider jwtProvider;

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
        validateTimes(startDay, endDay, availableStartTime, availableEndTime, duration, deadLine);

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

    private void validateTimes(LocalDate startDay, LocalDate endDay, LocalTime availableStartTime, LocalTime availableEndTime, LocalTime duration, LocalDateTime deadLine) {
        if (!(validateTime(availableStartTime) && validateTime(availableEndTime) && validateTime(duration))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "시간은 30분 단위로 입력 가능합니다.");
        }

        if (endDay.isBefore(startDay)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "가능날짜가 잘못되었습니다.");
        }
        if (availableEndTime.isBefore(availableStartTime) || availableEndTime.equals(availableStartTime)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "가능시간이 잘못되었습니다.");
        }
        if (LocalDateTime.now().isAfter(deadLine)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "마감시간이 잘못되었습니다.");
        }
        if (startDay.atStartOfDay().isBefore(deadLine)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "마감시간은 가능한 날짜보다 이전이어야 합니다.");
        }
    }

    private boolean validateTime(LocalTime time) {
        return time.getMinute() % 30 == 0;
    }

    public String getAccessToken(Room room) {
        String accessCode = room.getAccessCode();
        String token = jwtProvider.genToken(Map.of("accessCode", accessCode), 60 * 60 * 24 * 7);// 토큰 유효기간은 기본 일주일로
        return token;
    }

    public boolean validateToken(Room room, String accessToken) {
        String accessCode = room.getAccessCode();

        return jwtProvider.verify(accessToken)
                && jwtProvider.getClaims(accessToken).get("accessCode").equals(accessCode);
    }

    public Room getRoom(Long roomId) {
        return roomRepository
                .findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "모임을 찾을 수 없습니다."));
    }

    public void closeRoom(Long roomId) {
        Room room = getRoom(roomId);
        room.setDeadLine(LocalDateTime.now());
        roomRepository.save(room);
    }
}
