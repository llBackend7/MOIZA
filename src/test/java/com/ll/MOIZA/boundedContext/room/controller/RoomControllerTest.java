package com.ll.MOIZA.boundedContext.room.controller;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.MethodName.class)
class RoomControllerTest {

    @Autowired
    MockMvc mvc;

    @Test
    @WithUserDetails("user1")
    void 로그인사용자_방_만들기_페이지() throws Exception {
        ResultActions resultActions = mvc
                .perform(get("/room/create"))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(RoomController.class))
                .andExpect(handler().methodName("createRoom"))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    void 인증되지_않은_사용자_방_못만듦() throws Exception {
        ResultActions resultActions = mvc
                .perform(get("/room/create"))
                .andDo(print());
        resultActions
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithUserDetails("user1")
    void 방_생성_요청_POST() throws Exception {
        MultiValueMap<String, String> roomForm = new LinkedMultiValueMap<>();
        roomForm.put("name", List.of("테스트룸"));
        roomForm.put("description", List.of("설명"));
        roomForm.put("startDate", List.of(LocalDate.now().plusDays(4).toString()));
        roomForm.put("endDate", List.of(LocalDate.now().plusDays(10).toString()));
        roomForm.put("availableStartTime", List.of(LocalTime.of(13,30).toString()));
        roomForm.put("availableEndTime", List.of(LocalTime.of(15,30).toString()));
        roomForm.put("duration", List.of(LocalTime.of(0,30).toString()));
        roomForm.put("deadLine", List.of(LocalDateTime.now().plusDays(2).toString()));


        ResultActions resultActions = mvc
                .perform(post("/room/create").params(roomForm).with(csrf()))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(RoomController.class))
                .andExpect(handler().methodName("createRoom"))
                .andExpect(status().is2xxSuccessful());
    }
}