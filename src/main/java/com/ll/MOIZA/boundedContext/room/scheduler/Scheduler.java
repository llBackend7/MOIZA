package com.ll.MOIZA.boundedContext.room.scheduler;

import com.ll.MOIZA.base.mail.MailService;
import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import com.ll.MOIZA.boundedContext.room.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class Scheduler {
    private final RoomRepository roomRepository;
    private final MailService mailService;

    // 5분 간격으로 마감된 모임 탐색
    @Scheduled(fixedRate = 300000)
    public void checkConfirmedRoom() {
        List<Room> roomToConfirm = roomRepository.findByDeadLineBeforeAndMailSentFalse(LocalDateTime.now());
        roomToConfirm.forEach(room -> {
            room.getEnterRoom().forEach(enterRoom -> {
                Member member = enterRoom.getMember();
                mailService.sendMailTo(member, "<h1>%s 모임의 약속장소와 시간이 정해졌습니다.!</h1>".formatted(room.getName()));
            });
            room.setMailSent(true);
            roomRepository.save(room);
        });
    }
}
