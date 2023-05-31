package com.ll.MOIZA.boundedContext.room.controller;

import com.ll.MOIZA.base.mail.MailService;
import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.member.service.MemberService;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import com.ll.MOIZA.boundedContext.room.service.RoomService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/room")
public class RoomController {
    private final RoomService roomService;
    private final MemberService memberService;
    private final MailService mailService;

    @Data
    public static class RoomForm {
        @NotNull
        @NotEmpty
        String name;
        @NotNull
        @NotEmpty
        String description;
        @NotNull
        LocalDate startDate;
        @NotNull
        LocalDate endDate;
        @NotNull
        LocalTime availableStartTime;
        @NotNull
        LocalTime availableEndTime;
        @NotNull
        LocalTime duration;
        @NotNull
        LocalDateTime deadLine;
    }

    //TODO 로그인 기능 완성되면 주석해제
//    @PreAuthorize("isAuthenticated()")
    @GetMapping("/create")
    public String createRoom(RoomForm roomForm) {
        return "/room/create";
    }

    //TODO 로그인 기능 완성되면 주석해제
//    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    @ResponseBody
    public Map<String, Object> createRoom(@AuthenticationPrincipal User user,
                          @Valid RoomForm roomForm,
                          BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Map.of("error", bindingResult.getAllErrors());
        }

        //TODO 로그인 기능 완성되면 원래대로
        Member member = memberService.findByName("user1");
//        Member member = memberService.loginMember(user);
        Room room = roomService.createRoom(member,
                roomForm.name,
                roomForm.description,
                roomForm.startDate,
                roomForm.endDate,
                roomForm.availableStartTime,
                roomForm.availableEndTime,
                roomForm.duration,
                roomForm.deadLine);
        String accessToken = roomService.getAccessToken(room);
        Long roomId = room.getId();
        return Map.of("link", "http://localhost:8080/room/enter?roomId=%d&accessToken=%s".formatted(roomId,accessToken));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{roomId}/invite")
    @ResponseBody
    public String invite(@AuthenticationPrincipal User user,
                         @PathVariable Long roomId,
                         @RequestParam Long memberId) {
        Member actor = memberService.loginMember(user);
        Member friend = memberService.getMember(memberId);
        Room room = roomService.getRoom(roomId);

        String accessToken = roomService.getAccessToken(room);

        String mailContent = "<h1>%s님으로부터의 모임초대링크입니다.</h1>".formatted(actor.getName())
                + "<a href='http://localhost:8080/room/enter?roomId=%d&accessToken=%s'>모임참여하기</a>".formatted(roomId, accessToken);

        mailService.sendMailTo(friend, mailContent);

        return "{'result':'초대링크를 발송했습니다.'}";
    }
}
