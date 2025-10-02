package org.example.assmsof3022.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {
    @GetMapping({"/", "/calendar"})
    public String calendar() {
        return "calendar";
    }
}