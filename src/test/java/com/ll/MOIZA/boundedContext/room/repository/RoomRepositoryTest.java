package com.ll.MOIZA.boundedContext.room.repository;

import com.ll.MOIZA.boundedContext.room.entity.Room;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class RoomRepositoryTest {

    @Autowired
    RoomRepository roomRepository;

    @BeforeEach
    void 데이터셋_준비() {
        Room room1 = Room.builder()
                .name("needToSendMail1")
                .deadLine(LocalDateTime.now().minus(1, ChronoUnit.MINUTES))
                .build();

        Room room2 = Room.builder()
                .name("needToSendMail2")
                .deadLine(LocalDateTime.now().minus(2, ChronoUnit.MINUTES))
                .build();

        Room room3 = Room.builder()
                .name("needToSendMail3")
                .deadLine(LocalDateTime.now().plus(3, ChronoUnit.MINUTES))
                .build();

        Room room4 = Room.builder()
                .name("needToSendMail4")
                .deadLine(LocalDateTime.now().minus(3, ChronoUnit.MINUTES))
                .mailSent(true)
                .build();

        roomRepository.saveAll(List.of(room1, room2, room3, room4));
    }

    @Test
    void 메일보내야할_방_가져오기() {
        List<Room> roomsToSend = roomRepository.findByDeadLineBeforeAndMailSentFalse(LocalDateTime.now());
        assertThat(roomsToSend).hasSize(2);
        assertThat(roomsToSend.stream().map(Room::getName)).containsExactly("needToSendMail1", "needToSendMail2");
    }
}