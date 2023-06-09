package com.ll.MOIZA.boundedContext.chat.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.MOIZA.boundedContext.chat.document.Chat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class CachedChatRepositoryTest {
    @Autowired
    CachedChatRepository cachedChatRepository;

    @Autowired
    RedisTemplate<String, Chat> redisTemplate;

    @Autowired
    ObjectMapper objectMapper;

    @AfterEach
    void clear() {
        cachedChatRepository.clear("TEST");
    }

    @Test
    void redis직렬화_테스트() throws JsonProcessingException {
        Chat testChat = Chat.builder().roomId("TEST").content("테스트용").build();
        ZSetOperations<String, Chat> ops = redisTemplate.opsForZSet();

        ops.add("TEST", testChat, 10.0);

        Long offset = ops.reverseRank("TEST", testChat);
        assertThat(offset).isNotNull();

        redisTemplate.delete("TEST");
    }

    @Test
    void 채팅_저장() {
        Chat testChat = Chat.builder().roomId("TEST").content("테스트용").build();
        Chat save = cachedChatRepository.save(testChat);
    }

    @Test
    void 채팅_페이지네이션() throws JsonProcessingException {
        for (int i = 0; i < 55; i++) {
            Chat testChat = Chat.builder().roomId("TEST").memberId("%d".formatted(i)).content("테스트메시지%d".formatted(i)).build();
            cachedChatRepository.save(testChat);
        }

        Cursor<Chat> first = cachedChatRepository.findByRoom("TEST", null);
        Cursor<Chat> second = cachedChatRepository.findByRoom("TEST", first.getNextCursor());
        Cursor<Chat> third = cachedChatRepository.findByRoom("TEST", second.getNextCursor());

        assertThat(first).hasSize(20);
        assertThat(first.hasNext()).isTrue();
        assertThat(second).hasSize(20);
        assertThat(second.hasNext()).isTrue();
        assertThat(third).hasSize(15);
        assertThat(third.hasNext()).isFalse();
    }

    @Test
    void 채팅_페이지네이션_중간데이터삽입돼도_다음_불러오기에_영향_없어야함() throws JsonProcessingException {
        for (int i = 0; i < 55; i++) {
            Chat testChat = Chat.builder().roomId("TEST").memberId("%d".formatted(i)).content("테스트메시지%d".formatted(i)).build();
            cachedChatRepository.save(testChat);
        }

        Cursor<Chat> first = cachedChatRepository.findByRoom("TEST", null);
        Cursor<Chat> second = cachedChatRepository.findByRoom("TEST", first.getNextCursor());
        Chat testChat1 = Chat.builder().roomId("TEST").content("새로운채팅1").build();
        cachedChatRepository.save(testChat1);
        Chat testChat2 = Chat.builder().roomId("TEST").content("새로운채팅2").build();
        cachedChatRepository.save(testChat2);
        Cursor<Chat> third = cachedChatRepository.findByRoom("TEST", second.getNextCursor());

        assertThat(first).hasSize(20);
        assertThat(first.hasNext()).isTrue();
        assertThat(second).hasSize(20);
        assertThat(second.hasNext()).isTrue();
        assertThat(third).hasSize(15);
        assertThat(third.hasNext()).isFalse();

        assertThat(first.getContent().get(0).getCreateDate()).isAfter(third.getContent().get(0).getCreateDate());
    }
}