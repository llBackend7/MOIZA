package com.ll.MOIZA.boundedContext.home.controller;

import com.ll.MOIZA.base.rq.Rq;
import com.ll.MOIZA.boundedContext.member.service.MemberService;
import com.ll.MOIZA.boundedContext.room.entity.EnterRoom;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final MemberService memberService;
    private final Rq rq;

    @GetMapping("/")
    public String showMain(){
        if(rq.isLogout())
            return "home/login";
        else
            return "redirect:/groups";
    }

    @GetMapping("/login")
    public String login(HttpServletRequest request) {
        String uri = request.getHeader("Referer");
        if (uri != null && !uri.contains("/login")) {
            request.getSession().setAttribute("prevPage", uri);
        }

        return "home/login";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/groups")
    public String home(Model model){
        if(rq.isLogout())
            return "home/login";

        List<Room> rooms = rq.getMember().getEnterRooms().stream()
                .map(EnterRoom::getRoom)
                .sorted(Comparator.comparing(Room::getCreateDate).reversed().thenComparing(Room::getName))
                .collect(Collectors.toList());

        model.addAttribute("member", rq.getMember());
        model.addAttribute("rooms", rooms);
        return "home/main";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/invite")
    public String invite(@RequestParam Long roomId, Model model){
        model.addAttribute("roomId",roomId);
        return "home/invite";
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{groupId}")
    public String deleteGroup(@PathVariable Long groupId){
        memberService.deleteGroup(groupId);
        return "redirect:/groups";
    }
}
