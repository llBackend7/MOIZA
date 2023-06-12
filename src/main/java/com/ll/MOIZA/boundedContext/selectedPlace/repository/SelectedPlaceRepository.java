package com.ll.MOIZA.boundedContext.selectedPlace.repository;

import com.ll.MOIZA.boundedContext.room.entity.EnterRoom;
import com.ll.MOIZA.boundedContext.selectedPlace.entity.SelectedPlace;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SelectedPlaceRepository extends JpaRepository<SelectedPlace, Long> {

    @Query(value = "select sp.enterRoom from SelectedPlace as sp  where sp.name = ?1 ")
    List<EnterRoom> findEnterRoomsByPlaceName(String name);
}
