package com.ll.MOIZA.boundedContext.room.controller;

import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.member.service.MemberService;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import com.ll.MOIZA.boundedContext.room.service.EnterRoomService;
import com.ll.MOIZA.boundedContext.room.service.RoomService;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
@RequestMapping("/room")
public class EnterRoomController {

    private final RoomService roomService;
    private final EnterRoomService enterRoomService;
    private final MemberService memberService;

    @Data
    public static class SelectedDayWhitTimes {

        @NotNull
        LocalDate date;
        @NotNull
        LocalTime startTime;
        @NotNull
        LocalTime endTime;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/enter")
    public String enterRoom(@RequestParam long roomId, String accessToken, Model model) {
        Room room = roomService.getRoom(roomId);

        model.addAttribute("room", room);

        model.addAttribute("availableDayList", roomService.getAvailableDayList(roomId));
        model.addAttribute("availableTimeList", roomService.getAvailableTimeList(roomId));

        return "/room/enter";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/enter")
    @ResponseBody
    public void enterRoom(@AuthenticationPrincipal User user, @RequestParam long roomId,
            List<SelectedDayWhitTimes> selectedDayWhitTimeList, Model model) {
        Room room = roomService.getRoom(roomId);
        Member member = memberService.loginMember(user);
        enterRoomService.enterRoomWithSelectedTime(room, member, selectedDayWhitTimeList);
    }

}