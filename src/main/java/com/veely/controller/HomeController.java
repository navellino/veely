package com.veely.controller;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.veely.service.DashboardService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class HomeController {
	
	private final DashboardService dashboardService;
	
    @GetMapping({"/", "/welcome"})
    public String home(Model model) {
        model.addAttribute("metrics", dashboardService.getMetrics());
        return "welcome";   // template: src/main/resources/templates/welcome.html
    }

    @GetMapping("/login")
    public String login() {
        return "login";  // template: src/main/resources/templates/login.html
    }
}
