package com.ll.MOIZA.boundedContext.home.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/login")
    public String subIndex(){
        return "home/login";
    }

    @GetMapping("/groups")
    public String mainHome(){
        return "home/main";
    }
}
