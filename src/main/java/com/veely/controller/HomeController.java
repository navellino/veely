package com.veely.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping({"/", "/welcome"})
    public String home() {
        return "welcome";   // template: src/main/resources/templates/home.html
    }

    @GetMapping("/login")
    public String login() {
        return "login";  // template: src/main/resources/templates/login.html
    }
}
