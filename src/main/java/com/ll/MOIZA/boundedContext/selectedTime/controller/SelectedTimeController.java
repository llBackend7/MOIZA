package com.ll.MOIZA.boundedContext.selectedTime.controller;

import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.member.service.MemberService;
import com.ll.MOIZA.boundedContext.room.entity.EnterRoom;
import com.ll.MOIZA.boundedContext.room.service.EnterRoomService;
import com.ll.MOIZA.boundedContext.selectedTime.dto.SelectedTimeDto;
import com.ll.MOIZA.boundedContext.selectedTime.service.SelectedTimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class SelectedTimeController {
    private final SelectedTimeService selectedTimeService;
    private final EnterRoomService enterRoomService;
    private final MemberService memberService;

    @PostMapping("/room/{roomId}/date")
    public String createSelectedTime(@PathVariable Long roomId, @AuthenticationPrincipal User user, SelectedTimeDto selectedTimeDto) {
        Member member = memberService.findByName(user.getUsername());
        EnterRoom enterRoom = enterRoomService.findByMemberIdAndRoomIdElseThrow(member.getId(), roomId);
        selectedTimeService.addSelectedTime(selectedTimeDto, enterRoom);
        return "redirect:/room/" + roomId + "/date";
    }
}
