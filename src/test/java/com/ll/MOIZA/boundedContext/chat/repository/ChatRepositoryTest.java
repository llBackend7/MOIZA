package com.ll.MOIZA.boundedContext.chat.repository;

import com.ll.MOIZA.boundedContext.chat.document.Chat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ChatRepositoryTest {
    @Autowired
    ChatRepository chatRepository;

    @Test
    void 챗_생성() {
        Chat chat = Chat.builder()
                .memberId("1")
                .roomId("1")
                .writer("글쓴이")
                .content("테스트 채팅")
                .build();

        Chat save = chatRepository.save(chat);
        assertThat(save.getMemberId()).isEqualTo(chat.getMemberId());
        assertThat(save.getRoomId()).isEqualTo(chat.getRoomId());
        assertThat(save.getWriter()).isEqualTo(chat.getWriter());
        assertThat(save.getContent()).isEqualTo(chat.getContent());
    }

    @Test
    void 챗_가져오기() {
        Chat chat1 = Chat.builder()
                .memberId("1")
                .roomId("1")
                .writer("글쓴이")
                .content("테스트 채팅")
                .build();
        Chat chat2 = Chat.builder()
                .memberId("2")
                .roomId("1")
                .writer("글쓴이2")
                .content("테스트 채팅")
                .build();
        Chat chat3 = Chat.builder()
                .memberId("7")
                .roomId("3")
                .writer("글쓴이7")
                .content("테스트 채팅")
                .build();
        Chat chat4 = Chat.builder()
                .memberId("3")
                .roomId("4")
                .writer("글쓴이3")
                .content("테스트 채팅")
                .build();

        chatRepository.saveAll(List.of(chat1, chat2, chat3, chat4));
        List<Chat> chatOfRoom1 = chatRepository.findByRoomId("1");

        assertThat(chatOfRoom1.stream().map(Chat::getWriter)).contains("글쓴이", "글쓴이2");
    }
}