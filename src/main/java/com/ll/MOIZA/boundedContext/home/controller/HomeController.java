package com.ll.MOIZA.boundedContext.home.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/sub")
    public String subHome(){
        return "index_sub";
    }
}
