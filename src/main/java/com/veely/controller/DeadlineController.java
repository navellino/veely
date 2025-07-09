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
import com.veely.model.DocumentType;
import com.veely.service.EmploymentService;
import com.veely.service.DocumentService;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Controller
@RequestMapping("/deadlines")
@RequiredArgsConstructor
public class DeadlineController {

    private final DeadlineService deadlineService;
    private final VehicleService vehicleService;
    private final EmploymentService employmentService;
    private final DocumentService documentService;

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
        model.addAttribute("employmentDeadlines", deadlineService.getEmploymentDeadlines());
        model.addAttribute("vehicles", vehicleService.findAll());
        model.addAttribute("employments", employmentService.findAll());
        model.addAttribute("docTypes", DocumentType.values());
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
    
    @PostMapping("/employment/{id}")
    public String updateEmploymentDeadline(@PathVariable Long id,
                                           @RequestParam("date") String date) {
        LocalDate newDate = LocalDate.parse(date);
        employmentService.updateEndDate(id, newDate);
        return "redirect:/deadlines";
    }

    @PostMapping("/vehicle-docs")
    public String uploadVehicleDoc(@RequestParam("vehicleId") Long vehicleId,
                                   @RequestParam("file") MultipartFile file,
                                   @RequestParam("type") DocumentType type,
                                   @RequestParam(value = "issueDate", required = false) String issueDate,
                                   @RequestParam(value = "expiryDate", required = false) String expiryDate) throws IOException {
        LocalDate issued = (issueDate == null || issueDate.isBlank()) ? null : LocalDate.parse(issueDate);
        LocalDate exp = (expiryDate == null || expiryDate.isBlank()) ? null : LocalDate.parse(expiryDate);
        documentService.uploadVehicleDocument(vehicleId, file, type, issued, exp);
        return "redirect:/deadlines";
    }

    @PostMapping("/employment-docs")
    public String uploadEmploymentDoc(@RequestParam("employmentId") Long employmentId,
                                      @RequestParam("file") MultipartFile file,
                                      @RequestParam("type") DocumentType type,
                                      @RequestParam(value = "issueDate", required = false) String issueDate,
                                      @RequestParam(value = "expiryDate", required = false) String expiryDate) throws IOException {
        LocalDate issued = (issueDate == null || issueDate.isBlank()) ? null : LocalDate.parse(issueDate);
        LocalDate exp = (expiryDate == null || expiryDate.isBlank()) ? null : LocalDate.parse(expiryDate);
        documentService.uploadEmploymentDocument(employmentId, file, type, issued, exp);
        return "redirect:/deadlines";
    }
}
