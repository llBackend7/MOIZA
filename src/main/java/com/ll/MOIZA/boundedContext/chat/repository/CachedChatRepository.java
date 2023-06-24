package com.ll.MOIZA.boundedContext.chat.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.MOIZA.boundedContext.chat.document.Chat;
import jakarta.annotation.PostConstruct;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Repository
@Transactional
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

        try {
            operations.add(key, chat, -chat.getCreateDate().toInstant(ZoneOffset.UTC).toEpochMilli());

            //MAX_CHATS 넘으면 벌크삽입
            if (operations.size(key) >= MAX_CACHE) {
                Set<Chat> half = operations.range(key, MAX_CACHE / 2, -1);
                operations.removeRange(key, MAX_CACHE / 2, -1);
                chatRepository.saveAll(half);
            }
        }catch (RedisConnectionFailureException e){
            chatRepository.save(chat);
        }

        return chat;
    }

    @SneakyThrows
    public Cursor<Chat, String> findByRoom(String roomId, String currentCursorId) {
        String key = getKey(roomId);

        List<Chat> chats;

        Chat deserializedCursor = null;
        try {
            if (currentCursorId != null) {
                deserializedCursor = objectMapper.readValue(currentCursorId, Chat.class);
            }
            // 커서의 위치를 찾기 위해 커서 이전까지의 개수를 구함
            Long offset = getOffset(key, deserializedCursor);

            chats = fetchFromCache(key, offset);
        } catch (JsonProcessingException | RedisConnectionFailureException e) {
            // 이 경우 cursor는 redis의 value가 아니라 몽고db의 다큐멘트 id임.
            FetchedResult fetchedResult = fetchFromDb(PAGE_SIZE, roomId, currentCursorId);

            return Cursor.of(fetchedResult.fetchedChat, fetchedResult.hasNext, fetchedResult.nextCursorId);
        }

        // 몽고db에서 데이터를 더 긁어와야하는지 판단
        if (chats.size() > PAGE_SIZE) {
            Chat nextCursorChat = getNextCursorChat(chats);
            String nextCursorId = objectMapper.writeValueAsString(nextCursorChat);
            chats = chats.subList(0, PAGE_SIZE);

            return new Cursor<>(chats, PageRequest.of(0, PAGE_SIZE), true, nextCursorId);
        }

        // db에서 데이터 긁어와서 붙이기
        int deficiency = PAGE_SIZE - chats.size();
        FetchedResult fetchedResult = fetchFromDb(deficiency, roomId, currentCursorId);

        chats.addAll(fetchedResult.fetchedChat);
        return Cursor.of(chats, fetchedResult.hasNext, fetchedResult.nextCursorId);
    }

    private static class FetchedResult{
        boolean hasNext;
        String nextCursorId;
        List<Chat> fetchedChat = new ArrayList<>();
    }

    private FetchedResult fetchFromDb(int size, String roomId,  String cursorId){
        Pageable pageable = PageRequest.of(0, size + 1);
        List<Chat> chatsFromDb;
        FetchedResult fetchedResult = new FetchedResult();

        if (size == PAGE_SIZE && cursorId != null) {
            chatsFromDb = chatRepository.findByRoomIdWithCursor(roomId, new ObjectId(cursorId), pageable);
        }else{
            chatsFromDb = chatRepository.findByRoomId(roomId, pageable);
        }
        fetchedResult.fetchedChat = chatsFromDb;


        if (chatsFromDb.size() == size + 1) {
            fetchedResult.hasNext = true;
            fetchedResult.nextCursorId = chatsFromDb.get(size).getId();
            fetchedResult.fetchedChat = chatsFromDb.subList(0, size);
        }

        return fetchedResult;
    }

    private ArrayList<Chat> fetchFromCache(String key, Long offset) {
        return new ArrayList<>(Optional.ofNullable(operations.range(key, offset, offset + PAGE_SIZE))
                .orElse(new LinkedHashSet<>()));
    }

    private Chat getNextCursorChat(List<Chat> chats) {
        Chat nextCursorChat;
        int lastIndex = chats.size() - 1;

        if (lastIndex >= 0) {
            nextCursorChat = chats.get(lastIndex);
        } else {
            nextCursorChat = new Chat();
        }
        return nextCursorChat;
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
