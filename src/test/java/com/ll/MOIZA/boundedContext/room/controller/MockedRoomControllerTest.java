package com.ll.MOIZA.boundedContext.room.controller;

import com.ll.MOIZA.base.exception.GlobalExceptionHandler;
import com.ll.MOIZA.base.mail.MailService;
import com.ll.MOIZA.boundedContext.chat.repository.ChatRepository;
import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.member.service.MemberService;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import com.ll.MOIZA.boundedContext.room.service.RoomService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.data.mongodb.SpringDataMongoDB;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {RoomController.class, GlobalExceptionHandler.class})
@AutoConfigureDataMongo
@MockBean({JpaMetamodelMappingContext.class, SpringDataMongoDB.class})
public class MockedRoomControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    MemberService memberService;

    @MockBean
    RoomService roomService;

    @MockBean
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
                .email("freind@test.com")
                .build();
        when(memberService.getMember(anyLong())).thenReturn(friend);

        Room room = Room.builder()
                .accessCode("액세스코드")
                .build();
        when(roomService.getRoom(anyLong())).thenReturn(room);

        mockMvc.perform(get("/room/{roomId}/invite", 1L).param("memberId", "1"))
                .andExpect(status().isOk());

        verify(mailService, times(1)).sendMailTo(any(Member.class), any(String.class));
    }

    @Test
    @WithMockUser
    public void testDateTimeExceptionHandler() throws Exception {
        when(roomService.createRoom(any(), any(), any(), any(), any(), any(), any(), any(), any())).thenThrow(new DateTimeException("Test Exception"));

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();

        form.put("name", List.of("테스트룸"));
        form.put("description", List.of("설명"));
        form.put("startDate", List.of("2023-07-10"));
        form.put("endDate", List.of("2023-07-19"));
        form.put("availableStartTime", List.of("13:30"));
        form.put("availableEndTime", List.of("15:30"));
        form.put("duration", List.of("03:30"));
        form.put("deadLine", List.of(LocalDateTime.now().toString()));

        mockMvc
                .perform(post("/room/create")
                        .params(form)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }
}
