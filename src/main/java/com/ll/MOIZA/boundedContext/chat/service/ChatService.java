package com.ll.MOIZA.boundedContext.chat.service;

import com.ll.MOIZA.boundedContext.chat.document.Chat;
import com.ll.MOIZA.boundedContext.chat.repository.ChatRepository;
import com.ll.MOIZA.boundedContext.chat.repository.Cursor;
import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.StringEscapeUtils;
import org.bson.types.ObjectId;
import org.springframework.data.domain.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepository chatRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final int PAGE_SIZE = 20;

    public Chat sendChat(Member member, Room room, String content) {
        return sendChat(member.getId().toString(), member.getName(), member.getProfile(), room.getId().toString(), content);
    }


    public Cursor<Chat> getChats(Room room, String cursor) {
        List<Chat> rooms;
        Pageable pageable = PageRequest.of(0, PAGE_SIZE + 1);
        if (cursor == null) {
            rooms = chatRepository.findByRoomId(room.getId().toString(), pageable);
        } else {
            rooms = chatRepository.findByRoomIdWithCursor(room.getId().toString(), new ObjectId(cursor), pageable);
        }

        if (rooms.size() == PAGE_SIZE + 1) {
            return new Cursor<>(
                    rooms.subList(0, PAGE_SIZE),
                    PageRequest.of(0, PAGE_SIZE),
                    true,
                    rooms.get(PAGE_SIZE).getId());
        }

        return new Cursor<>(rooms, PageRequest.of(0, PAGE_SIZE), false, null);
    }

    public Chat sendChat(String memberId, String username, String profile, String roomId, String content) {
        Chat chat = Chat.builder()
                .roomId(roomId)
                .memberId(memberId)
                .writer(username)
                .profile(profile)
                .content(StringEscapeUtils.escapeHtml4(content))
                .build();

        chat = chatRepository.save(chat);
        messagingTemplate.convertAndSend("/room/%s".formatted(roomId), chat); // /room/{roomId}

        return chat;
    }
}
