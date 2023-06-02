package com.ll.MOIZA.boundedContext.room.scheduler;

import com.ll.MOIZA.base.mail.MailService;
import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import com.ll.MOIZA.boundedContext.room.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional
public class Scheduler {
    private final RoomRepository roomRepository;
    private final MailService mailService;
    private final SpringTemplateEngine templateEngine;

    // 2분 간격으로 마감된 모임 탐색
    @Scheduled(fixedRate = 120_000)
    public void checkConfirmedRoom() {
        List<Room> roomToConfirm = roomRepository.findByDeadLineBeforeAndMailSentFalse(LocalDateTime.now());
        roomToConfirm.forEach(room -> {
            room.getEnterRoom().forEach(enterRoom -> {
                Member member = enterRoom.getMember();
                mailService.sendMailTo(member, buildMailContent(room.getName(),room.getId()));
            });
            room.setMailSent(true);
            roomRepository.save(room);
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
