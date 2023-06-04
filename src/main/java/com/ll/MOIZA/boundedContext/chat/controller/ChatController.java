package com.ll.MOIZA.boundedContext.chat.controller;

import com.ll.MOIZA.boundedContext.chat.document.Chat;
import com.ll.MOIZA.boundedContext.chat.service.ChatService;
import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.member.service.MemberService;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import com.ll.MOIZA.boundedContext.room.service.EnterRoomService;
import com.ll.MOIZA.boundedContext.room.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
public class ChatController {
    private final MemberService memberService;
    private final RoomService roomService;
    private final ChatService chatService;

    private final EnterRoomService enterRoomService;

    @MessageMapping("/{roomId}") // /send/{roomId}
    public Chat send(@Payload String content,
                     @Headers MessageHeaders headers,
                     Principal principal) {
        SimpMessageHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(headers, SimpMessageHeaderAccessor.class);
        String username = principal.getName();
        String destination = accessor.getDestination();
        String roomId = destination.substring(destination.lastIndexOf("/") + 1);
        Member member = memberService.findByName(username);
        Room room = roomService.getRoom(Long.parseLong(roomId));

        validateRoomMember(member, room);

        Chat chat = chatService.sendChat(member, room, content);

        return chat;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/chats")
    public Slice<Chat> loadChats(@RequestParam(defaultValue = "0") int page,
                                 @RequestParam Long roomId,
                                 @AuthenticationPrincipal User user) {
        Room room = roomService.getRoom(roomId);
        Member member = memberService.loginMember(user);

        validateRoomMember(member,room);

        return chatService.getChats(room, page);
    }

    private void validateRoomMember(Member member, Room room) {
        if (enterRoomService.isNotRoomMember(room, member)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "채팅접근 권한이 없습니다.");
        }
    }
}
