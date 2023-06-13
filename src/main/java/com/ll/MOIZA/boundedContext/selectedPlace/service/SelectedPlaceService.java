package com.ll.MOIZA.boundedContext.selectedPlace.service;

import com.ll.MOIZA.boundedContext.room.entity.EnterRoom;
import com.ll.MOIZA.boundedContext.room.entity.Room;

import com.ll.MOIZA.boundedContext.selectedPlace.entity.SelectedPlace;
import com.ll.MOIZA.boundedContext.selectedPlace.repository.SelectedPlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SelectedPlaceService {

    private final SelectedPlaceRepository selectedPlaceRepository;

    @Transactional
    public SelectedPlace CreateSelectedPlace(
            String name,
            EnterRoom enterRoom
    ) {
        SelectedPlace selectedPlace = SelectedPlace.builder()
                .name(name)
                .enterRoom(enterRoom)
                .build();

        if (!selectedPlaceRepository.findEnterRoomsByPlaceName(name).contains(enterRoom)) {
            return selectedPlaceRepository.save(selectedPlace);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "중복된 투표입니다.");
        }
    }

    public Map<SelectedPlace, Long> getSelectedPlaces(Room room) {
        List<EnterRoom> enterRooms = room.getEnterRoom();
        Map<SelectedPlace, Long> selectedPlaceMap = enterRooms.stream()
                .flatMap(enterRoom -> enterRoom.getSelectedPlaces().stream())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<SelectedPlace, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        return selectedPlaceMap;
    }
}