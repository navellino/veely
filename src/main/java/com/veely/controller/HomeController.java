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
        model.addAttribute("vehicleStatusLabels", dashboardService.getVehicleStatusCounts().keySet());
        model.addAttribute("vehicleStatusValues", dashboardService.getVehicleStatusCounts().values());
        model.addAttribute("fuelCosts", dashboardService.getFuelCosts(6));
        model.addAttribute("reportTotals", dashboardService.getExpenseReportTotals(6));
        model.addAttribute("upcomingDeadlines", dashboardService.getUpcomingDeadlines(5));
        model.addAttribute("pendingReports", dashboardService.getPendingExpenseReports(5));
        return "welcome";   // template: src/main/resources/templates/welcome.html
    }

    @GetMapping("/login")
    public String login() {
        return "login";  // template: src/main/resources/templates/login.html
    }
}
