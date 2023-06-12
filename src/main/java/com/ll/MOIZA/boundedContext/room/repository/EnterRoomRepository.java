package com.ll.MOIZA.boundedContext.room.repository;

import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.room.entity.EnterRoom;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;

public interface EnterRoomRepository extends JpaRepository<EnterRoom, Long> {
    Optional<EnterRoom> findByRoomAndMember(Room room, Member member);

    @Query(value = "select er.member from EnterRoom as er where er.room = ?1 order by er.member")
    List<Member> findMembersByRoom(Room room);

    Optional<EnterRoom> findByMemberIdAndRoomId(Long memberId, Long roomId);
}
