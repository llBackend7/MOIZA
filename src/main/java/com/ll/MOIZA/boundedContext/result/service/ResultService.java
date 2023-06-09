package com.ll.MOIZA.boundedContext.result.service;

import com.ll.MOIZA.boundedContext.result.entity.DecidedResult;
import com.ll.MOIZA.boundedContext.result.repository.ResultRepository;
import com.ll.MOIZA.boundedContext.room.entity.Room;
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
    public DecidedResult createResult(String place, Room room) {
        DecidedResult result = DecidedResult.builder()
                                            .decidedDayTime(LocalDateTime.now()) // TODO: 시간 현황 페이지 만들어지고 나서 고쳐야함.
                                            .decidedPlace(place)
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
