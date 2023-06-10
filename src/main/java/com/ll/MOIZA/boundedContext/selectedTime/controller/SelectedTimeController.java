package com.ll.MOIZA.boundedContext.selectedTime.controller;

import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.selectedTime.service.SelectedTimeService;
import com.ll.MOIZA.boundedContext.selectedTime.service.TimeRangeWithMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/selectedTime")
public class SelectedTimeController {

    @GetMapping("/")
    public String updateMembers(@RequestParam("time") TimeRangeWithMember timeRange, Model model) {
        List<Member> members = timeRange.getMembers();
        model.addAttribute("members", members);
        return "status/date";
    }
}
