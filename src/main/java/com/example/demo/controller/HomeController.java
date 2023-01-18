package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "raw";
    }

    @GetMapping("/no")
    public String no() {
        return "no-kurento";
    }

    @GetMapping("/yes")
    public String yes() {
        return "kurento-client";
    }
}
