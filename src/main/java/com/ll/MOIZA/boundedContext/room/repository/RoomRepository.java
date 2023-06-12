package com.ll.MOIZA.boundedContext.room.repository;


import com.ll.MOIZA.boundedContext.room.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByDeadLineBeforeAndMailSentFalse(LocalDateTime currentTime);
    List<Room> findByDeadLineBefore(LocalDateTime currentTime);
}
