package com.ll.MOIZA.boundedContext.room.controller;

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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    void 로그인사용자_방_만들기() throws Exception {
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
                .andExpect(status().is4xxClientError());
    }
}