package com.ll.MOIZA.boundedContext.chat.repository;

import com.ll.MOIZA.boundedContext.chat.document.Chat;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChatRepository extends MongoRepository<Chat, String> {
    List<Chat> findByRoomId(String roomId);
}
