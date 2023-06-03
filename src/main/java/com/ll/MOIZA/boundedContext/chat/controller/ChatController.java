package com.ll.MOIZA.boundedContext.chat.controller;

import com.ll.MOIZA.boundedContext.chat.document.Chat;
import com.ll.MOIZA.boundedContext.chat.service.ChatService;
import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.member.service.MemberService;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import com.ll.MOIZA.boundedContext.room.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
public class ChatController {
    private final MemberService memberService;
    private final RoomService roomService;
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/{roomId}") // /send/{roomId}
    public Chat send(@Payload String content,
                     @Headers MessageHeaders headers,
                     Principal principal) {
        SimpMessageHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(headers, SimpMessageHeaderAccessor.class);
        String username = principal.getName();
        String destination = accessor.getDestination();
        String roomId = destination.substring(destination.lastIndexOf("/")+1);
        Member member = memberService.findByName(username);
        Room room = roomService.getRoom(Long.parseLong(roomId));

        Chat chat = chatService.saveChat(member, room, content);

        messagingTemplate.convertAndSend("/room/%s".formatted(roomId), chat); // /room/{roomId}
        return chat;
    }
}
