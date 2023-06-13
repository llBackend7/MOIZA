package com.ll.MOIZA.boundedContext.result.service;

import com.ll.MOIZA.boundedContext.member.entity.Member;
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
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ResultService {

    private final ResultRepository resultRepository;

    @Transactional
    public void createResult(Room room, TimeRangeWithMember timeRangeWithMember, String decidedPlace) {
        LocalDateTime decidedDateTime = null;
        List<Member> participationMembers = null;
        List<Member> nonParticipationMembers = null;

        if (timeRangeWithMember != null) {
            decidedDateTime = (timeRangeWithMember.getDate() != null) ? timeRangeWithMember.getDate().atTime(timeRangeWithMember.getStart()) : null;
            participationMembers = (timeRangeWithMember.getParticipationMembers() != null) ? timeRangeWithMember.getParticipationMembers() : null;
            nonParticipationMembers = (timeRangeWithMember.getNonParticipationMembers() != null) ? timeRangeWithMember.getNonParticipationMembers() : null;
        }

        DecidedResult result = DecidedResult.builder()
                .decidedDayTime(decidedDateTime)
                .decidedPlace(decidedPlace)
                .participationMembers(participationMembers)
                .nonParticipationMembers(nonParticipationMembers)
                .room(room)
                .build();

        resultRepository.save(result);
    }

    public DecidedResult getResult(Long roomId) {
        return resultRepository
                .findByRoomId(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "모임의 결과를 찾을 수 없습니다."));
    }
}
