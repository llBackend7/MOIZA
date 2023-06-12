package com.ll.MOIZA.boundedContext.chat.repository;

import com.ll.MOIZA.boundedContext.chat.document.Chat;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatRepository extends MongoRepository<Chat, String> {
    List<Chat> findByRoomId(String roomId);

    @Query(value = "{ 'roomId' : ?0, '_id' : { $lte: ?1 } }", sort = "{ 'createDate' : -1 }")
    List<Chat> findByRoomIdWithCursor(String roomId, ObjectId cursor, Pageable pageable);

    @Query(value = "{ 'roomId' : ?0 }", sort = "{ 'createDate' : -1 }")
    List<Chat> findByRoomId(String roomId, Pageable pageable);
}
