package com.ll.MOIZA.boundedContext.chat.service;

import com.ll.MOIZA.boundedContext.chat.document.Chat;
import com.ll.MOIZA.boundedContext.chat.repository.ChatRepository;
import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepository chatRepository;
    private final SimpMessagingTemplate messagingTemplate;
    public Chat sendChat(Member member, Room room, String content) {
        return sendChat(member.getId().toString(), member.getName(), member.getProfile(), room.getId().toString(), content);
    }


    public Slice<Chat> getChats(Room room, int page) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("createDate"));
        Pageable pageable = PageRequest.of(page, 20, Sort.by(sorts));
        return chatRepository.findByRoomId(room.getId().toString(), pageable);
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
