package com.ll.MOIZA.boundedContext.chat.service;

import com.ll.MOIZA.boundedContext.chat.document.Chat;
import com.ll.MOIZA.boundedContext.chat.repository.ChatRepository;
import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import com.ll.MOIZA.boundedContext.room.repository.EnterRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepository chatRepository;
    private final SimpMessagingTemplate messagingTemplate;
    public Chat sendChat(Member member, Room room, String content) {
        Chat chat = Chat.builder()
                .roomId(room.getId().toString())
                .memberId(member.getId().toString())
                .writer(member.getName())
                .content(content)
                .build();

        messagingTemplate.convertAndSend("/room/%s".formatted(room.getId()), chat); // /room/{roomId}

        return chatRepository.save(chat);
    }


    public Slice<Chat> getChats(Room room, int page) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("createDate"));
        Pageable pageable = PageRequest.of(page, 20, Sort.by(sorts));
        return chatRepository.findByRoomId(room.getId().toString(), pageable);
    }
}
