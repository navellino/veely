package com.veely.controller;

import com.veely.entity.Correspondence;
import com.veely.model.CorrespondenceType;
import com.veely.service.CorrespondenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequestMapping("/correspondence")
@RequiredArgsConstructor
public class CorrespondenceController {
    private final CorrespondenceService service;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("items", service.getAll());
        model.addAttribute("types", CorrespondenceType.values());
        return "correspondence/index";
    }

    @PostMapping
    public String create(@RequestParam("tipo") CorrespondenceType tipo,
                         @RequestParam("descrizione") String descrizione,
                         @RequestParam(value = "data", required = false) String data) {
        LocalDate d = (data == null || data.isBlank()) ? LocalDate.now() : LocalDate.parse(data);
        Correspondence c = service.register(tipo, descrizione, d);
        // For now we don't show the protocol to user except maybe a flash attribute (not implemented)
        return "redirect:/correspondence";
    }
}