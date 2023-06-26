package com.ll.MOIZA.boundedContext.chat.controller;

import com.ll.MOIZA.base.security.CustomOAuth2User;
import com.ll.MOIZA.boundedContext.chat.document.Chat;
import com.ll.MOIZA.boundedContext.chat.service.ChatService;
import com.ll.MOIZA.boundedContext.room.service.RoomService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.data.mongodb.SpringDataMongoDB;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@WebMvcTest(ChatController.class)
@AutoConfigureMockMvc
@AutoConfigureDataMongo
@MockBean({JpaMetamodelMappingContext.class, SpringDataMongoDB.class})
@ActiveProfiles("test")
class ChatControllerTest {
    @Autowired
    MockMvc mvc;

    @MockBean
    RoomService roomService;

    @MockBean
    ChatService chatService;

    @Autowired
    ChatController chatController;

    @Test
    @DisplayName("메시지 보내기")
    void send() {
        String content = "Test Message";
        String roomId = "1";
        Long memberId = 1L;
        String username = "user1";
        String profile = "TestProfile";

        Object[] objects = prepareMock(username, roomId, memberId, profile);
        MessageHeaders headers = (MessageHeaders) objects[0];
        Authentication auth = (Authentication) objects[1];
        Chat expectedChat = (Chat) objects[2];

        Chat actualChat = chatController.send(content, headers, auth);

        assertThat(expectedChat.getContent()).isEqualTo(actualChat.getContent());
    }

    Object[] prepareMock(String username, String roomId, Long memberId, String profile ){
        // mock 생성
        CustomOAuth2User customOAuth2User = mock(CustomOAuth2User.class);
        when(customOAuth2User.getUsername()).thenReturn(username);
        when(customOAuth2User.getId()).thenReturn(memberId);
        when(customOAuth2User.getProfile()).thenReturn(profile);

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(username);


        SimpMessageHeaderAccessor accessor = mock(SimpMessageHeaderAccessor.class);
        mockStatic(MessageHeaderAccessor.class)
                .when(() -> MessageHeaderAccessor.getAccessor(any(MessageHeaders.class), any()))
                .thenReturn(accessor);
        when(accessor.getDestination()).thenReturn("/app.send/1");

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(customOAuth2User);

        MessageHeaders headers = mock(MessageHeaders.class);
        when(headers.get(any())).thenReturn("/app/send/" + roomId);

        Chat expectedChat = new Chat();
        when(chatService.sendChat(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(expectedChat);

        return new Object[]{headers, auth, expectedChat};
    }
}