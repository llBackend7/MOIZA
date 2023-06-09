package com.ll.MOIZA.boundedContext.selectedTime.controller;

import com.ll.MOIZA.boundedContext.room.entity.Room;
import com.ll.MOIZA.boundedContext.room.service.RoomService;
import com.ll.MOIZA.boundedContext.selectedTime.service.SelectedTimeService;
import com.ll.MOIZA.boundedContext.selectedTime.service.SelectedTimeService.TimeRangeWithMember;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/timeRanges")
public class SelectedTimeController {

    private final SelectedTimeService selectedTimeService;

    private final RoomService roomService;

    //    @PreAuthorize("isAuthenticated()")
    @GetMapping()
    public List<TimeRangeWithMember> getOverlappingTimeRanges(@RequestParam long roomId, LocalDate date) {
        Room room = roomService.getRoom(roomId);
        return selectedTimeService.findOverlappingTimeRanges(room, date);
    }
}
