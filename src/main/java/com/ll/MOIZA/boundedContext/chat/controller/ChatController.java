package com.ll.MOIZA.boundedContext.chat.controller;

import com.ll.MOIZA.base.security.CustomOAuth2User;
import com.ll.MOIZA.boundedContext.chat.document.Chat;
import com.ll.MOIZA.boundedContext.chat.repository.Cursor;
import com.ll.MOIZA.boundedContext.chat.service.ChatService;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import com.ll.MOIZA.boundedContext.room.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
public class ChatController {
    private final RoomService roomService;
    private final ChatService chatService;

    @MessageMapping("/{roomId}") // /send/{roomId}
    public Chat send(@Payload String content,
                     @Headers MessageHeaders headers,
                     Principal principal) {
        UserDetails userDetails = (UserDetails) ((Authentication) principal).getPrincipal();

        Long memberId = ((CustomOAuth2User) userDetails).getId();
        String username = userDetails.getUsername();
        String profile = ((CustomOAuth2User) userDetails).getProfile();

        String roomId = extractRoomId(headers);

        Chat chat = chatService.sendChat(memberId.toString(), username, profile, roomId, content);

        return chat;
    }

    @PreAuthorize("isAuthenticated() && hasAuthority('ROOM#' + #roomId + '_MEMBER')")
    @GetMapping("/chats")
    public Cursor<Chat, String> loadChats(@RequestParam(required = false) String nextCursor, @RequestParam Long roomId) {
        Room room = roomService.getRoom(roomId);
        return chatService.getChats(room, nextCursor);
    }

    private String extractRoomId(MessageHeaders headers) {
        SimpMessageHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(headers, SimpMessageHeaderAccessor.class);
        String destination = accessor.getDestination();
        String roomId = destination.substring(destination.lastIndexOf("/") + 1);
        return roomId;
    }
}
