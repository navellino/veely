package com.veely.controller;

import com.veely.entity.Project;
import com.veely.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/settings/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("projects", projectService.findAll());
        return "settings/projects/index";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("project", new Project());
        return "settings/projects/form";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("project") Project project, BindingResult binding) {
        if (binding.hasErrors()) {
            return "settings/projects/form";
        }
        Project saved = projectService.create(project);
        return "redirect:/settings/projects/" + saved.getId() + "/edit";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("project", projectService.findByIdOrThrow(id));
        return "settings/projects/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("project") Project project, BindingResult binding) {
        if (binding.hasErrors()) {
            return "settings/projects/form";
        }
        projectService.update(id, project);
        return "redirect:/settings/projects/" + id + "/edit";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        projectService.delete(id);
        return "redirect:/settings/projects";
    }
}
