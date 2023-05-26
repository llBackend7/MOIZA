package com.ll.MOIZA.boundedContext.room.controller;

import com.ll.MOIZA.base.mail.MailConfig;
import com.ll.MOIZA.base.mail.MailService;
import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.member.service.MemberService;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import com.ll.MOIZA.boundedContext.room.service.RoomService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import({MailService.class, MailConfig.class})
@Disabled
public class MockedRoomControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    MemberService memberService;

    @MockBean
    RoomService roomService;

    @Autowired
    MailService mailService;

    /*
    실제 메일 날라옴
    테스트는 성공
     */
    @Test
    @WithMockUser
    void 초대링크_발송_테스트() throws Exception {
        Member actor = Member.builder()
                .name("actor")
                .email("me@meme.com")
                .build();
        when(memberService.loginMember(any())).thenReturn(actor);

        Member friend = Member.builder()
                .name("friend")
                .email("mouse6051@gmail.com")
                .build();
        when(memberService.getMember(anyLong())).thenReturn(friend);

        Room room = Room.builder()
                .accessCode("액세스코드")
                .build();
        when(roomService.getRoom(anyLong())).thenReturn(room);

        mockMvc.perform(get("/room/{roomId}/invite", 1L).param("memberId", "1"))
                .andExpect(status().isOk());
    }
}
