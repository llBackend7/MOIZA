package com.ll.MOIZA.boundedContext.chat.controller;

import com.ll.MOIZA.boundedContext.chat.document.Chat;
import com.ll.MOIZA.boundedContext.chat.service.ChatService;
import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.member.service.MemberService;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import com.ll.MOIZA.boundedContext.room.service.RoomService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ChatController {
    private final MemberService memberService;
    private final RoomService roomService;
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/{roomId}") // /send/{roomId}
    public Chat send(@Payload String content, @Headers MessageHeaders headers) {
        SimpMessageHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(headers, SimpMessageHeaderAccessor.class);

        String destination = accessor.getDestination();
        String roomId = destination.substring(destination.lastIndexOf("/")+1);

        Chat chat = Chat.builder().content("asd").writer("qqq").build();
        messagingTemplate.convertAndSend("/room/%s".formatted(roomId), chat); // /room/{roomId}
        return chat;
    }
}
