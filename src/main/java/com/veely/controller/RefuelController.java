package com.veely.controller;

import com.veely.entity.Refuel;
import com.veely.service.FuelCardService;
import com.veely.service.RefuelService;
import com.veely.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/fleet/refuels")
@RequiredArgsConstructor
public class RefuelController {

    private final RefuelService refuelService;
    private final VehicleService vehicleService;
    private final FuelCardService fuelCardService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("refuels", refuelService.findAll());
        return "fleet/refuels/index";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("refuel", new Refuel());
        addOptions(model);
        return "fleet/refuels/form";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("refuel") Refuel refuel,
                         BindingResult binding, Model model) {
        if (binding.hasErrors()) {
            addOptions(model);
            return "fleet/refuels/form";
        }
        Refuel saved = refuelService.create(refuel);
        return "redirect:/fleet/refuels/" + saved.getId() + "/edit";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Refuel r = refuelService.findByIdOrThrow(id);
        model.addAttribute("refuel", r);
        addOptions(model);
        return "fleet/refuels/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("refuel") Refuel refuel,
                         BindingResult binding, Model model) {
        if (binding.hasErrors()) {
            addOptions(model);
            return "fleet/refuels/form";
        }
        refuelService.update(id, refuel);
        return "redirect:/fleet/refuels/" + id + "/edit";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        refuelService.delete(id);
        return "redirect:/fleet/refuels";
    }

    private void addOptions(Model model) {
        model.addAttribute("vehicles", vehicleService.findAll());
        model.addAttribute("fuelCards", fuelCardService.findAll());
    }
}
