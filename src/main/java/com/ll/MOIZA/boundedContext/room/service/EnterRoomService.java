package com.ll.MOIZA.boundedContext.room.service;

import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.room.controller.RoomController;
import com.ll.MOIZA.boundedContext.room.controller.RoomController.SelectedDayWithTime;
import com.ll.MOIZA.boundedContext.room.entity.EnterRoom;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import com.ll.MOIZA.boundedContext.room.repository.EnterRoomRepository;
import com.ll.MOIZA.boundedContext.selectedTime.service.SelectedTimeService;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.hibernate.mapping.Collection;
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

        List<RoomController.SelectedDayWithTime> nonOverlappingTimeList = removeOverlappingTime(selectedDayWhitTimesList);

        for (RoomController.SelectedDayWithTime selectedDayWhitTime : nonOverlappingTimeList) {
            selectedTimeService.CreateSelectedTime(
                    selectedDayWhitTime.getDate(),
                    selectedDayWhitTime.getStartTime(),
                    selectedDayWhitTime.getEndTime(),
                    enterRoom
            );
        }

        return enterRoom;
    }

    private List<RoomController.SelectedDayWithTime> sortList(
            List<RoomController.SelectedDayWithTime> inputList) {

        List<RoomController.SelectedDayWithTime> sortedList = new ArrayList<>(inputList);

        sortedList.sort(Comparator.comparing(RoomController.SelectedDayWithTime::getStartTime));

        return sortedList;
    }
    public List<RoomController.SelectedDayWithTime> removeOverlappingTime (List<RoomController.SelectedDayWithTime> inputList){

        List<RoomController.SelectedDayWithTime> sortedList = new ArrayList<>(sortList(inputList));

        List<RoomController.SelectedDayWithTime> nonOverlapping = new ArrayList<>();

        RoomController.SelectedDayWithTime prevInterval = null;

        for (RoomController.SelectedDayWithTime interval : sortedList) {
            if (prevInterval == null) {
                prevInterval = interval;
                nonOverlapping.add(interval);
            } else {
                LocalTime prevEndTime = prevInterval.getEndTime();
                LocalTime currentStartTime = interval.getStartTime();
                LocalTime currentEndTime = interval.getEndTime();

                if (currentStartTime.isAfter(prevEndTime)) {
                    nonOverlapping.add(interval);
                    prevInterval = interval;
                } else if (currentEndTime.isAfter(prevEndTime)) {
                    prevInterval.setEndTime(currentEndTime);
                }
            }
        }

        return nonOverlapping;
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