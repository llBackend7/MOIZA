package com.ll.MOIZA.boundedContext.chat.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.MOIZA.boundedContext.chat.document.Chat;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.bson.types.ObjectId;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class CachedChatRepository {
    private final RedisTemplate<String, Chat> redisTemplate;
    private final ObjectMapper objectMapper;
    private final ChatRepository chatRepository;
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
        if (operations.size(key) >= MAX_CACHE) {
            Set<Chat> half = operations.range(key, MAX_CACHE / 2, -1);
            operations.removeRange(key, MAX_CACHE / 2, -1);
            chatRepository.saveAll(half);
        }
        return chat;
    }

    @SneakyThrows
    public Cursor<Chat> findByRoom(String roomId, String cursor) {
        String key = getKey(roomId);

        List<Chat> chats;
        boolean hasNext;
        String nextCursor;

        try {
            Chat deserializedCursor = null;

            if (cursor != null) {
                deserializedCursor = objectMapper.readValue(cursor, Chat.class);
            }

            // 커서의 위치를 찾기 위해 커서 이전까지의 개수를 구함
            Long offset = getOffset(key, deserializedCursor);

            Set<Chat> chatSet = Optional.ofNullable(operations.range(key, offset, offset + PAGE_SIZE))
                    .orElse(new LinkedHashSet<>());

            hasNext = chatSet.size() > PAGE_SIZE;
            chats = new ArrayList<>(chatSet);
            Chat nextCursorChat = chats.get(chats.size() - 1);
            nextCursor = objectMapper.writeValueAsString(nextCursorChat);

            if (hasNext) {
                chats = chats.subList(0, PAGE_SIZE);
            } else {
                // db에서 데이터 긁어와서 붙이기
                int deficiency = PAGE_SIZE - chats.size();

                Pageable pageable = PageRequest.of(0, deficiency + 1);

                List<Chat> appendChats;
                appendChats = chatRepository.findByRoomId(roomId, pageable);

                if (appendChats.size() == deficiency + 1) {
                    hasNext = true;
                    nextCursor = appendChats.get(deficiency).getId();
                    appendChats = appendChats.subList(0, deficiency);
                } else {
                    hasNext = false;
                    nextCursor = null;
                }

                chats.addAll(appendChats);
            }
        } catch (JsonProcessingException e) {
            // 이 경우 cursor는 redis의 value가 아니라 몽고db의 다큐멘트 id임.
            Pageable pageable = PageRequest.of(0, PAGE_SIZE + 1);
            chats = chatRepository.findByRoomIdWithCursor(roomId, new ObjectId(cursor), pageable);

            if (chats.size() == PAGE_SIZE + 1) {
                hasNext = true;
                nextCursor = chats.get(PAGE_SIZE).getId();
                chats = chats.subList(0, PAGE_SIZE);
            } else {
                hasNext = false;
                nextCursor = null;
            }
        }


        return new Cursor<>(chats, PageRequest.of(0, PAGE_SIZE), hasNext, nextCursor);
    }

    private Long getOffset(String key, Chat deserializedCursor) {
        Long offset = operations.rank(key, deserializedCursor);

        if (offset == null) {
            offset = 0L; // 커서가 존재하지 않는 경우, 첫 페이지로 설정
        }
        return offset;
    }

    private String getKey(String roomId) {
        return "ROOM#%s_CHAT".formatted(roomId);
    }

    public void clear(String roomId) {
        redisTemplate.delete("ROOM#%s_CHAT".formatted(roomId));
    }
}