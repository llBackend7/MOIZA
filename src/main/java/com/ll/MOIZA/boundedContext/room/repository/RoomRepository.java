package com.ll.MOIZA.boundedContext.room.repository;


import com.ll.MOIZA.boundedContext.room.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {
}
