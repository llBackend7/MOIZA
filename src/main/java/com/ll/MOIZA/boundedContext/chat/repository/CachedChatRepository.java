package com.ll.MOIZA.boundedContext.chat.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.MOIZA.boundedContext.chat.document.Chat;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Repository;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class CachedChatRepository {
    private final RedisTemplate<String, Chat> redisTemplate;
    private final ObjectMapper objectMapper;
    private ZSetOperations<String, Chat> operations;

    private static final int PAGE_SIZE = 20;

    private static final int MAX_CACHE = 500;

    @PostConstruct
    public void init() {
        operations = redisTemplate.opsForZSet();
    }

    public Chat save(Chat chat) {
        chat.setCreateDate(LocalDateTime.now());
        chat.setModifyDate(LocalDateTime.now());

        String key = getKey(chat.getRoomId());

        operations.add(key, chat, -chat.getCreateDate().toInstant(ZoneOffset.UTC).toEpochMilli());

        //TODO MAX_CHATS 넘으면 벌크삽입

        return chat;
    }

    public Cursor<Chat> findByRoom(String roomId, String serializedChat) throws JsonProcessingException {
        String key = getKey(roomId);

        Chat deserializedCursor = null;
        if (serializedChat != null) {
            deserializedCursor = objectMapper.readValue(serializedChat, Chat.class);
        }

        // 커서의 위치를 찾기 위해 커서 이전까지의 개수를 구함
        Long offset = operations.rank(key, deserializedCursor);

        if (offset == null) {
            offset = 0L; // 커서가 존재하지 않는 경우, 첫 페이지로 설정
        }

        Set<Chat> chatSet = Optional.ofNullable(operations.range(key, offset, offset + PAGE_SIZE))
                .orElse(new LinkedHashSet<>());


        boolean hasNext = chatSet.size() > PAGE_SIZE;

        List<Chat> chats = new ArrayList<>(chatSet);
        Chat nextCursorChat = chats.get(chats.size() - 1);
        String serializedNextCursor = objectMapper.writeValueAsString(nextCursorChat);

        if (hasNext) {
            chats = chats.subList(0, PAGE_SIZE);
        }

        return new Cursor<>(chats, PageRequest.of(0, PAGE_SIZE), hasNext, serializedNextCursor);
    }

    private String getKey(String roomId) {
        return "ROOM#%s_CHAT".formatted(roomId);
    }

    public void clear(String roomId) {
        redisTemplate.delete("ROOM#%s_CHAT".formatted(roomId));
    }
}
