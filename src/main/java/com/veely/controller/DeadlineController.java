package com.veely.controller;

import com.veely.model.DeadlineItem;
import com.veely.service.DeadlineService;
import com.veely.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/deadlines")
@RequiredArgsConstructor
public class DeadlineController {

    private final DeadlineService deadlineService;
    private final VehicleService vehicleService;

    @GetMapping
    public String list(Model model) {
        List<DeadlineItem> items = deadlineService.getVehicleDeadlines();
        Map<String, List<DeadlineItem>> grouped = new LinkedHashMap<>();
        grouped.put("Assicurazione", new ArrayList<>());
        grouped.put("Bollo", new ArrayList<>());
        grouped.put("Leasing", new ArrayList<>());
        grouped.put("Fuel Card", new ArrayList<>());
        for (DeadlineItem d : items) {
            grouped.computeIfAbsent(d.category(), k -> new ArrayList<>()).add(d);
        }
        model.addAttribute("vehicleDeadlines", grouped);
        model.addAttribute("employmentDeadlines", List.of());
        return "deadlines/index";
    }

    @PostMapping("/vehicle/{id}/{type}")
    public String updateVehicleDeadline(@PathVariable Long id,
                                        @PathVariable String type,
                                        @RequestParam("date") String date) {
        LocalDate newDate = LocalDate.parse(date);
        switch (type) {
		        case "insurance" -> vehicleService.updateInsuranceExpiry(id, newDate);
		        case "carTax"   -> vehicleService.updateCarTaxExpiry(id, newDate);
		        case "fuelCard" -> vehicleService.updateFuelCardExpiry(id, newDate);
		        case "lease"    -> vehicleService.updateLeaseExpiry(id, newDate);
		        default -> {}
        }
        return "redirect:/deadlines";
    }
}
