package com.ll.MOIZA.boundedContext.room.scheduler;

import com.ll.MOIZA.base.mail.MailService;
import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.result.entity.DecidedResult;
import com.ll.MOIZA.boundedContext.result.repository.ResultRepository;
import com.ll.MOIZA.boundedContext.result.service.ResultService;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import com.ll.MOIZA.boundedContext.room.repository.RoomRepository;
import com.ll.MOIZA.boundedContext.selectedPlace.entity.SelectedPlace;
import com.ll.MOIZA.boundedContext.selectedPlace.service.SelectedPlaceService;
import com.ll.MOIZA.boundedContext.selectedTime.service.SelectedTimeService;
import com.ll.MOIZA.boundedContext.selectedTime.service.TimeRangeWithMember;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Transactional
public class Scheduler {
    private final RoomRepository roomRepository;
    private final MailService mailService;
    private final SpringTemplateEngine templateEngine;
    private final SelectedTimeService selectedTimeService;
    private final SelectedPlaceService selectedPlaceService;
    private final ResultRepository resultRepository;

    // 2분 간격으로 마감된 모임 탐색
    @Scheduled(fixedRate = 120_000)
    public void checkConfirmedRoom() {
        List<Room> roomToConfirm = roomRepository.findByDeadLineBeforeAndMailSentFalse(LocalDateTime.now());

        roomToConfirm.forEach(room -> {
            List<TimeRangeWithMember> overlappingTimes = selectedTimeService.findOverlappingTimeRanges(room);
            LocalDateTime decidedDateTime = null;
            TimeRangeWithMember timeRangeWithMember = null;
            List<Member> availableMembers = null;
            List<Member> unavailableMembers = null;
            String place="";

            if (!overlappingTimes.isEmpty()) {
                timeRangeWithMember = overlappingTimes.get(0);

                Map<SelectedPlace, Long> selectedPlaces = selectedPlaceService.getSelectedPlaces(room);
                Map.Entry<SelectedPlace, Long> firstPlaceEntry = selectedPlaces.entrySet().stream().findFirst().orElse(null);
                if (firstPlaceEntry != null) {
                    SelectedPlace firstSelectedPlace = firstPlaceEntry.getKey();
                    place = firstSelectedPlace.getName();
                }

                decidedDateTime = timeRangeWithMember.getDate().atTime(timeRangeWithMember.getStart());
                availableMembers = timeRangeWithMember.getParticipationMembers();
                unavailableMembers = timeRangeWithMember.getNonParticipationMembers();
            }

            DecidedResult result = DecidedResult.builder()
                    .decidedDayTime(decidedDateTime)
                    .decidedPlace(place)
                    .participationMembers(availableMembers)
                    .nonParticipationMembers(unavailableMembers)
                    .room(room)
                    .build();

            resultRepository.save(result);

            try {
                room.getEnterRoom().forEach(enterRoom -> {
                    Member member = enterRoom.getMember();
                        mailService.sendMailTo(member, buildMailContent(room.getName(),room.getId()));

                });
                room.setMailSent(true);
                roomRepository.save(room);
            } catch (Exception ignored){}
        });
    }

    private String buildMailContent(String roomName, Long id) {
        Context context = new Context();
        context.setVariable("roomName", roomName);
        context.setVariable("id", id);

        String content = templateEngine.process("/mail/mail-template", context);
        return content;
    }
}
