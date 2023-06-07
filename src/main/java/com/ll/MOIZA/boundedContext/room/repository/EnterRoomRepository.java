package com.ll.MOIZA.boundedContext.room.repository;

import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.room.entity.EnterRoom;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EnterRoomRepository extends JpaRepository<EnterRoom, Long> {
    Optional<EnterRoom> findByRoomAndMember(Room room, Member member);
}
