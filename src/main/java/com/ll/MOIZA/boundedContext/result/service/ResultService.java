package com.ll.MOIZA.boundedContext.result.service;

import com.ll.MOIZA.boundedContext.result.entity.DecidedResult;
import com.ll.MOIZA.boundedContext.result.repository.ResultRepository;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import com.ll.MOIZA.boundedContext.selectedTime.service.TimeRangeWithMember;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ResultService {

    private final ResultRepository resultRepository;

    @Transactional
    public DecidedResult createResult(Room room, TimeRangeWithMember timeRangeWithMember, String decidedPlace) {
        LocalDateTime decidedDateTime = timeRangeWithMember.getDate().atTime(timeRangeWithMember.getStart());

        DecidedResult result = DecidedResult.builder()
                .decidedDayTime(decidedDateTime)
                .decidedPlace(decidedPlace)
                .room(room)
                .build();

        resultRepository.save(result);
        return result;
    }

    public DecidedResult getResult(Long roomId) {
        return resultRepository
                .findByRoomId(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "모임을 찾을 수 없습니다."));
    }
}
