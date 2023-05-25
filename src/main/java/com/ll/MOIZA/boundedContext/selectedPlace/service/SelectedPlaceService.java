package com.ll.MOIZA.boundedContext.room.service;

import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.room.entity.EnterRoom;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import com.ll.MOIZA.boundedContext.room.repository.EnterRoomRepository;
import com.ll.MOIZA.boundedContext.selectedPlace.entity.SelectedPlace;
import com.ll.MOIZA.boundedContext.selectedTime.entity.SelectedTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EnterRoomService {

    private final EnterRoomRepository enterRoomRepository;

    @Transactional
    public EnterRoom enterRoom(Room room,
            Member member,
            List<SelectedTime> timeList,
            List<SelectedPlace> placesList
    ) {
        EnterRoom enterRoom = EnterRoom.builder()
                .room(room)
                .member(member)
                .selectedTimes(timeList)
                .selectedPlaces(placesList)
                .build();

        return enterRoomRepository.save(enterRoom);
    }

    @Transactional
    public EnterRoom enterRoom(Room room,
            Member member,
            List<SelectedTime> timeList
    ) {
        EnterRoom enterRoom = EnterRoom.builder()
                .room(room)
                .member(member)
                .selectedTimes(timeList)
                .build();

        return enterRoomRepository.save(enterRoom);
    }
}