package com.ll.MOIZA.boundedContext.selectedTime.controller;

import com.ll.MOIZA.boundedContext.room.entity.Room;
import com.ll.MOIZA.boundedContext.room.service.RoomService;
import com.ll.MOIZA.boundedContext.selectedTime.service.SelectedTimeService;
import com.ll.MOIZA.boundedContext.selectedTime.service.TimeRangeWithMember;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/timeRanges")
public class SelectedTimeController {

    private final SelectedTimeService selectedTimeService;

    private final RoomService roomService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping()
    public String getOverlappingTimeRanges(@RequestParam long roomId, Model model) {
        Room room = roomService.getRoom(roomId);
        List<TimeRangeWithMember> timeWithMember = selectedTimeService.findOverlappingTimeRanges(room);

        model.addAttribute("room", room);
        model.addAttribute("timeWithMember", timeWithMember);

        return "room/status";
    }
}