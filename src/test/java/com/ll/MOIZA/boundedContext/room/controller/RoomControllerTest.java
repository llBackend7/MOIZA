package com.ll.MOIZA.boundedContext.room.controller;

import com.ll.MOIZA.boundedContext.room.controller.RoomController;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import com.ll.MOIZA.boundedContext.room.service.RoomService;
import com.ll.MOIZA.boundedContext.room.entity.EnterRoom;
import com.ll.MOIZA.boundedContext.selectedPlace.service.SelectedPlaceService;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.any;
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

    @Autowired
    RoomService roomService;

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

    @Test
    @DisplayName("시간 현황 페이지의 View Attributes 확인")
    @WithUserDetails("user1")
    void showRoomDateTest() throws Exception {
        // Arrange
        Long roomId = 1L;

        // Assert
        ResultActions resultActions = mvc
                .perform(get("/room/"+roomId+"/date").with(csrf()).param("timeIndex","0"))
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(view().name("status/date"))
                .andExpect(model().attributeExists("room"))
                .andExpect(model().attributeExists("actor"))
                .andExpect(model().attributeExists("overlappingTimes"))
                .andExpect(model().attributeExists("selectedAvailableMembers"))
                .andExpect(model().attributeExists("selectedUnavailableMembers"));
    }

    @Test
    @DisplayName("모임 결과 페이지")
    @WithUserDetails("user1")
    void showResultTest() throws Exception {
        long roomId = 2L;

        ResultActions resultActions = mvc
                .perform(get("/room/"+roomId+"/result").with(csrf()))
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(view().name("room/result"))
                .andExpect(model().attributeExists("result"))
                .andExpect(model().attributeExists("appKey"));
    }

    @Test
    @DisplayName("모임 마감 POST request")
    @WithUserDetails("user1")
    void closeRoomTest() throws Exception {
        long roomId = 1L;

        ResultActions resultActions = mvc
                .perform(
                        post("/room/"+roomId+"/close")
                                .with(csrf())
                )
                .andDo(print());

        resultActions
                .andExpect(handler().methodName("closeRoom"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/room/" + roomId + "/result"));
    }

    @Test
    @DisplayName("장소 투표 페이지의 View Attributes 확인")
    @WithUserDetails("user1")
    void showRoomPlaceTest() throws Exception {
        Long roomId = 1L;

        ResultActions resultActions = mvc
                .perform(get("/room/"+roomId+"/place").with(csrf()).param("timeIndex","0"))
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(view().name("status/place"))
                .andExpect(model().attributeExists("selectedPlaceMap"))
                .andExpect(model().attributeExists("room"))
                .andExpect(model().attributeExists("actor"));
    }

    @Test
    @DisplayName("방 입장 화면_정상_토큰")
    @WithUserDetails("user1")
    void show_enterRoom_page() throws Exception {
        Long roomId = 1L;
        Room room = roomService.getRoom(roomId);

        String accessToken = roomService.getAccessToken(room);

        ResultActions resultActions = mvc
                .perform(get("/room/enter?roomId=%d&accessToken=%s".formatted(roomId, accessToken))
                        .with(csrf())
                )
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(view().name("room/enter"));
    }

    @Test
    @DisplayName("방 입장 화면_비정상_토큰")
    @WithUserDetails("user1")
    void show_enterRoom_page_exception() throws Exception {
        Long roomId = 1L;

        String accessToken = "not_access_token";

        ResultActions resultActions = mvc
                .perform(get("/room/enter?roomId=%d&accessToken=%s".formatted(roomId, accessToken))
                        .with(csrf())
                )
                .andDo(print());

        resultActions
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("추가 시간 테스트")
    @WithUserDetails("user1")
    void changeTime_test() throws Exception {
        Long roomId = 1L;

        ResultActions resultActions = mvc
                .perform(get("/room/%d/changeTime".formatted(roomId))
                        .with(csrf()));

        resultActions
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("장소 투표 페이지의 POST")
    @WithUserDetails("user1")
    void showRoomPlacePostTest() throws Exception {
        Long roomId = 1L;

        ResultActions resultActions = mvc
                .perform(
                        post("/room/"+roomId+"/place")
                                .with(csrf())
                )
                .andDo(print());

        resultActions
                .andExpect(handler().methodName("createPlace"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/room/" + roomId + "/place"));
    }
}