package com.ll.MOIZA.boundedContext.chat.service;

import com.ll.MOIZA.boundedContext.chat.document.Chat;
import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.member.service.MemberService;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import com.ll.MOIZA.boundedContext.room.service.RoomService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ChatServiceTest {
    @Autowired
    MemberService memberService;

    @Autowired
    RoomService roomService;

    @Autowired
    ChatService chatService;

    @Autowired
    MongoTemplate mongoTemplate;

    @AfterEach
    void 클리어() {
        mongoTemplate.dropCollection("chat");
    }

    @Test
    void 채팅_보내기() {
        Member user1 = memberService.findByName("user1");
        Room room = roomService.getRoom(1L);

        Chat sentChat = chatService.sendChat(user1, room, "TEST CHAT");

        assertThat(sentChat).isNotNull();
    }

    @Test
    void 가장_최근_채팅_20개씩_가져오기() {
        Member user1 = memberService.findByName("user1");
        Room room = roomService.getRoom(1L);
        for (int i = 0; i < 100; i++) {
            chatService.sendChat(user1, room, "TEST CHAT%s".formatted(i));
        }

        Slice<Chat> chats = chatService.getChats(room, 0);

        assertThat(chats.getSize()).isEqualTo(20);
        assertThat(chats.hasPrevious()).isFalse();
        assertThat(chats.hasNext()).isTrue();
    }
}