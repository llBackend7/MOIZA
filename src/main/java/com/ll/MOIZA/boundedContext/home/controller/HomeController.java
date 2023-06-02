package com.ll.MOIZA.boundedContext.home.controller;

import com.ll.MOIZA.base.rq.Rq;
import com.ll.MOIZA.boundedContext.member.service.MemberService;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final MemberService memberService;
    private final Rq rq;

    @GetMapping("/login")
    public String subLogin(){
        return "home/login";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/groups")
    public String mainHome(Model model){
        List<Room> rooms = rq.getMember().getRooms();
        model.addAttribute("member", rq.getMember());
        model.addAttribute("rooms", rooms);
        return "home/main";
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{groupId}")
    public String deleteGroup(@PathVariable Long groupId){
        memberService.deleteGroup(groupId);
        return "redirect:/groups";
    }
}
