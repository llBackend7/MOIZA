package com.ll.MOIZA.boundedContext.selectedPlace.service;

import com.ll.MOIZA.boundedContext.room.entity.EnterRoom;
import com.ll.MOIZA.boundedContext.selectedPlace.entity.SelectedPlace;
import com.ll.MOIZA.boundedContext.selectedPlace.repository.SelectedPlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        return selectedPlaceRepository.save(selectedPlace);
    }
}