package com.ll.MOIZA.boundedContext.room.service;

import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.room.controller.RoomController;
import com.ll.MOIZA.boundedContext.room.entity.EnterRoom;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import com.ll.MOIZA.boundedContext.room.repository.EnterRoomRepository;
import com.ll.MOIZA.boundedContext.selectedTime.service.SelectedTimeService;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${custom.site.calculatorUrl}")
    private String calculatorUrl;

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
    public EnterRoom enterRoomWithSelectedTime(Room room, Member member,
            List<RoomController.SelectedDayWithTime> selectedDayWhitTimesList) {
        EnterRoom enterRoom = enter(room, member);
        enterRoomRepository.delete(enterRoom);
        enterRoom = enter(room, member);

        for (RoomController.SelectedDayWithTime selectedDayWhitTime : selectedDayWhitTimesList) {
            selectedTimeService.CreateSelectedTime(
                    selectedDayWhitTime.getDate(),
                    selectedDayWhitTime.getStartTime(),
                    selectedDayWhitTime.getEndTime(),
                    enterRoom
            );
        }

        return enterRoom;
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

    public void clearCache(Room room) throws IOException {
        URL url = new URL(calculatorUrl + "/refresh-cache/" + room.getId());

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");

        int responseCode = connection.getResponseCode();

        System.out.println("responseCode:" + responseCode);

        connection.disconnect();
    }

    public EnterRoom enter(Room room, Member member) {
        Long memberId = member.getId();
        Long roomId = room.getId();
        return findByMemberIdAndRoomId(memberId, roomId).orElse(createEnterRoom(room,member));
    }

    public EnterRoom findByMemberIdAndRoomIdElseThrow(Long memberId, Long roomId) {
        return enterRoomRepository.findByMemberIdAndRoomId(memberId, roomId).orElseThrow();
    }
}