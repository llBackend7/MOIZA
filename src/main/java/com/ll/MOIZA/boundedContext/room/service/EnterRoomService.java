package com.ll.MOIZA.boundedContext.room.service;

import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.room.controller.RoomController;
import com.ll.MOIZA.boundedContext.room.entity.EnterRoom;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import com.ll.MOIZA.boundedContext.room.repository.EnterRoomRepository;
import com.ll.MOIZA.boundedContext.selectedTime.service.SelectedTimeService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EnterRoomService {

    private final EnterRoomRepository enterRoomRepository;
    private final SelectedTimeService selectedTimeService;

    @Transactional
    public EnterRoom createEnterRoom(Room room,
            Member member
    ) {
        Optional<EnterRoom> opEnterRoom = enterRoomRepository.findByRoomAndMember(room, member);
        if (opEnterRoom.isPresent()) {
            return opEnterRoom.get();
        }

        EnterRoom enterRoom = EnterRoom.builder()
                .room(room)
                .member(member)
                .build();

        return enterRoomRepository.save(enterRoom);
    }

    @Transactional
    public void enterRoomWithSelectedTime(Room room, Member member,
            List<RoomController.SelectedDayWithTime> selectedDayWhitTimesList) {

        EnterRoom enterRoom = createEnterRoom(room, member);

        for (RoomController.SelectedDayWithTime selectedDayWhitTime : selectedDayWhitTimesList) {
            selectedTimeService.CreateSelectedTime(
                    selectedDayWhitTime.getDate(),
                    selectedDayWhitTime.getStartTime(),
                    selectedDayWhitTime.getEndTime(),
                    enterRoom
            );
        }
    }
    public Optional<EnterRoom> findByMemberIdAndRoomId(Long memberId, Long roomId) {
        return enterRoomRepository.findByMemberIdAndRoomId(memberId, roomId);
    }

    @Transactional
    public void leaveEnterRoom(Room room, Member actor) {
        Optional<EnterRoom> opEnterRoom = enterRoomRepository.findByRoomAndMember(room, actor);
        if(opEnterRoom.isPresent())
            enterRoomRepository.delete(opEnterRoom.get());
        else
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
}