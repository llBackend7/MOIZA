package com.ll.MOIZA.boundedContext.chat.repository;

import com.ll.MOIZA.boundedContext.chat.document.Chat;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChatRepository extends MongoRepository<Chat, String> {
    List<Chat> findByRoomId(String roomId);

    Slice<Chat> findByRoomId(String roomId, Pageable pageable);
}
