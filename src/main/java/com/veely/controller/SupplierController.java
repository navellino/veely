package com.veely.controller;

import com.veely.entity.Supplier;
import com.veely.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/fleet/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("suppliers", supplierService.findAll());
        return "fleet/suppliers/index";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("supplier", new Supplier());
        return "fleet/suppliers/form";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute Supplier supplier, BindingResult binding) {
        if (binding.hasErrors()) {
            return "fleet/suppliers/form";
        }
        Supplier saved = supplierService.create(supplier);
        return "redirect:/fleet/suppliers/" + saved.getId() + "/edit";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Supplier s = supplierService.findByIdOrThrow(id);
        model.addAttribute("supplier", s);
        return "fleet/suppliers/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id, @Valid @ModelAttribute Supplier supplier,
                         BindingResult binding) {
        if (binding.hasErrors()) {
            return "fleet/suppliers/form";
        }
        supplierService.update(id, supplier);
        return "redirect:/fleet/suppliers/" + id + "/edit";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        supplierService.delete(id);
        return "redirect:/fleet/suppliers";
    }
}
