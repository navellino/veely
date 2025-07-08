package com.veely.controller;

import com.veely.model.DeadlineItem;
import com.veely.model.DocumentType;
import com.veely.service.DeadlineService;
import com.veely.service.DocumentService;
import com.veely.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/deadlines")
@RequiredArgsConstructor
public class DeadlineController {

    private final DeadlineService deadlineService;
    private final VehicleService vehicleService;
    private final DocumentService documentService;

    @GetMapping
    public String list(Model model) {
        List<DeadlineItem> items = deadlineService.getVehicleDeadlines();
        model.addAttribute("deadlines", items);
        return "deadlines/index";
    }

    @PostMapping("/vehicle/{id}/{type}")
    public String updateVehicleDeadline(@PathVariable Long id,
                                        @PathVariable String type,
                                        @RequestParam("date") String date,
                                        @RequestParam(value = "file", required = false) MultipartFile file) throws IOException {
        LocalDate newDate = LocalDate.parse(date);
        switch (type) {
            case "insurance" -> {
                vehicleService.updateInsuranceExpiry(id, newDate);
                if (file != null && !file.isEmpty()) {
                    documentService.uploadVehicleDocument(id, file, DocumentType.INSURANCE, LocalDate.now(), newDate);
                }
            }
            case "carTax" -> {
                vehicleService.updateCarTaxExpiry(id, newDate);
                if (file != null && !file.isEmpty()) {
                    documentService.uploadVehicleDocument(id, file, DocumentType.OTHER, LocalDate.now(), newDate);
                }
            }
            case "fuelCard" -> {
                vehicleService.updateFuelCardExpiry(id, newDate);
                if (file != null && !file.isEmpty()) {
                    documentService.uploadVehicleDocument(id, file, DocumentType.OTHER, LocalDate.now(), newDate);
                }
            }
            default -> {}
        }
        return "redirect:/deadlines";
    }
}
