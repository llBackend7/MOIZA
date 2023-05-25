package com.ll.MOIZA.boundedContext.selectedTime.service;

import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.room.entity.EnterRoom;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import com.ll.MOIZA.boundedContext.room.repository.EnterRoomRepository;
import com.ll.MOIZA.boundedContext.selectedPlace.entity.SelectedPlace;
import com.ll.MOIZA.boundedContext.selectedTime.entity.SelectedTime;
import com.ll.MOIZA.boundedContext.selectedTime.repository.SelectedTimeRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        SelectedTime selectedTime = SelectedTime.builder()
                .date(day)
                .startTime(startTime)
                .endTime(endTime)
                .enterRoom(enterRoom)
                .build();

        return selectedTimeRepository.save(selectedTime);
    }
}