package com.ll.MOIZA.boundedContext.room.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.MOIZA.base.calendar.service.CalendarService;
import com.ll.MOIZA.base.exception.GlobalExceptionHandler;
import com.ll.MOIZA.base.mail.MailService;
import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.member.service.MemberService;
import com.ll.MOIZA.boundedContext.result.service.ResultService;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import com.ll.MOIZA.boundedContext.room.service.EnterRoomService;
import com.ll.MOIZA.boundedContext.room.service.RoomService;
import com.ll.MOIZA.boundedContext.selectedPlace.service.SelectedPlaceService;
import com.ll.MOIZA.boundedContext.selectedTime.service.SelectedTimeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.data.mongodb.SpringDataMongoDB;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {RoomController.class, GlobalExceptionHandler.class})
@AutoConfigureDataMongo
@Import({MockedRoomControllerTest.TestController.class})
@MockBean({JpaMetamodelMappingContext.class, SpringDataMongoDB.class})
public class MockedRoomControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    MemberService memberService;

    @MockBean
    RoomService roomService;

    @MockBean
    MailService mailService;

    @MockBean
    ResultService resultService;

    @MockBean
    EnterRoomService enterRoomService;

    @MockBean
    SelectedTimeService selectedTimeService;

    @MockBean
    SelectedPlaceService selectedPlaceService;

    @MockBean
    CalendarService calendarService;

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

    @Test
    @WithMockUser(username = "testUser")
    void 방_입장시_권한_추가() throws Exception {
        Room room = new Room();
        Member member = new Member();
        List<RoomController.SelectedDayWithTime> selectedDayWhitTimeList = Arrays.asList(
                new RoomController.SelectedDayWithTime(LocalDate.now(), LocalTime.now(), LocalTime.now().plusHours(1))
        );

        String selectedDaysJSON = objectMapper.writeValueAsString(selectedDayWhitTimeList);

        when(roomService.getRoom(anyLong())).thenReturn(room);
        when(roomService.validateToken(eq(room), anyString())).thenReturn(true);
        when(memberService.loginMember(any(User.class))).thenReturn(member);
        when(enterRoomService.enterRoomWithSelectedTime(eq(room), eq(member), anyList())).thenReturn(null);

        mockMvc.perform(post("/room/enter")
                        .param("roomId", "1")
                        .param("accessToken", "accessToken")
                        .content(selectedDaysJSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(jsonPath("$.redirectUrl", is("/room/1/place")));

        MvcResult mvcResult = mockMvc.perform(get("/test-authority")
                        .param("authority", "ROOM#1_MEMBER")
                        .with(csrf()))
                .andReturn();

        boolean hasAuthority = Boolean.parseBoolean(mvcResult.getResponse().getContentAsString());

        assertThat(hasAuthority).isTrue();
    }

    @Test
    @WithMockUser
    @DisplayName("방 초대 테스트")
    void inviteTest() throws Exception {
        Room dummyRoom = new Room();

        when(roomService.getRoom(anyLong())).thenReturn(dummyRoom);
        when(roomService.getAccessToken(any(Room.class))).thenReturn("ACCESS_TOKEN");

        mockMvc.perform(get("/room/1/invite"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("*/room/enter*"));
    }

    @Test
    @WithMockUser(authorities = "ROOM#1_MEMBER")
    @DisplayName("채팅방 페이지 보여주기")
    void showRoomChat() throws Exception {
        Room dummyRoom = Room.builder().id(1l).leader(new Member()).build();
        Member dummyActor = new Member();

        when(roomService.getRoom(anyLong())).thenReturn(dummyRoom);
        when(memberService.loginMember(any(User.class))).thenReturn(dummyActor);

        mockMvc.perform(get("/room/1/chat"))
                .andExpect(status().isOk())
                .andExpect(view().name("status/chat"));
    }

    /**
     * 테스트전용 컨트롤러
     * 사용자 권한을 확인하기 위함
     */
    @RestController
    public static class TestController {
        @GetMapping("/test-authority")
        public ResponseEntity<Boolean> hasAuthority(@AuthenticationPrincipal User user, @RequestParam String authority) {
            boolean hasAuthority = user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(authority));
            return ResponseEntity.ok(hasAuthority);
        }
    }
}