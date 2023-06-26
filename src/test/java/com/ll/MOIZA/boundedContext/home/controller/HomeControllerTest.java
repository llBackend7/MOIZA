package com.ll.MOIZA.boundedContext.home.controller;

import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.member.service.MemberService;
import com.ll.MOIZA.boundedContext.room.service.EnterRoomService;
import com.ll.MOIZA.boundedContext.room.service.RoomService;
import org.junit.jupiter.api.DisplayName;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.MethodName.class)
class HomeControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private MemberService memberService;

    @Test
    @DisplayName("메인페이지: 로그아웃 상태일 시 로그인 페이지 반환")
    void showMain_logout() throws Exception {
        mvc.perform(get("/"))
                .andExpect(handler().methodName("showMain"))
                .andExpect(status().isOk())
                .andExpect(view().name("home/login"));
    }

    @Test
    @DisplayName("메인페이지: 로그인 상태일 시 그룹페이지로 리다이렉트")
    @WithUserDetails("user1")
    void showMain_login() throws Exception {
        mvc.perform(get("/"))
                .andExpect(handler().methodName("showMain"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/groups"));
    }

    @Test
    @DisplayName("이전 페이지 세션 저장 여부")
    @WithUserDetails("user1")
    void checkReferer() throws Exception {
        String referer = "home/invite";

        mvc.perform(get("/login").header("Referer", referer))
                .andExpect(status().isOk())
                .andExpect(view().name("home/login"))
                .andExpect(request().sessionAttribute("prevPage", referer));
    }

    @Test
    @DisplayName("메인 뷰와 방 목록 표시")
    @WithUserDetails("user1")
    void showHome() throws Exception {
        Member member = memberService.findByName("user1");
        assertThat(member.getEnterRooms().size()).isEqualTo(2);

        mvc.perform(get("/groups"))
                .andExpect(handler().methodName("home"))
                .andExpect(status().isOk())
                .andExpect(view().name("home/main"));
    }

    @Test
    @DisplayName("모임 삭제")
    @WithUserDetails("user1")
    void deleteGroup() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                    delete("/"+String.valueOf(1L))
                            .with(csrf())
                )
                .andDo(print());

        resultActions
                .andExpect(handler().methodName("deleteGroup"))
                        .andExpect(status().is3xxRedirection())
                        .andExpect(redirectedUrl("/groups"));

        Member member = memberService.findByName("user1");
        assertThat(member.getEnterRooms().size()).isEqualTo(1);
    }

    @Test
    @DisplayName("모임 나가기")
    @WithUserDetails("user1")
    void leaveGroup() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/leave")
                            .with(csrf())
                            .param("roomId", "2")
                )
                .andDo(print());

        resultActions
            .andExpect(handler().methodName("leaveGroup"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/groups"));

        Member member = memberService.findByName("user1");
        assertThat(member.getEnterRooms().size()).isEqualTo(1);
    }
}
