package com.ll.MOIZA.boundedContext.room.service;

import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.room.entity.EnterRoom;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import com.ll.MOIZA.boundedContext.room.repository.EnterRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EnterRoomService {

    private final EnterRoomRepository enterRoomRepository;

    @Transactional
    public EnterRoom createEnterRoom(Room room,
            Member member
    ) {
        EnterRoom enterRoom = EnterRoom.builder()
                .room(room)
                .member(member)
                .build();

        return enterRoomRepository.save(enterRoom);
    }

    public boolean isNotRoomMember(Room room, Member member) {
        return !isRoomMember(room, member);
    }

    private boolean isRoomMember(Room room, Member member) {
        return enterRoomRepository.findByRoomAndMember(room, member).isPresent();
    }
}