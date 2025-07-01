package com.veely.controller;

import com.veely.entity.Employment;
import com.veely.model.DocumentType;
import com.veely.model.EmploymentStatus;
import com.veely.model.JobRole;
import com.veely.service.DocumentService;
import com.veely.service.EmploymentService;
import com.veely.service.EmployeeService;
import org.springframework.core.io.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;

@Controller
@RequestMapping("/fleet/employments")
@RequiredArgsConstructor
public class EmploymentController {

    private final EmploymentService employmentService;
    private final EmployeeService employeeService;
    private final DocumentService documentService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("employments", employmentService.findAll());
        return "fleet/employments/index";
    }
    
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("employment", new Employment());
        //model.addAttribute("employees", employeeService.findAll());
        model.addAttribute("employees", employeeService.findAvailableForEmployment());
        model.addAttribute("docTypes", DocumentType.values());
        model.addAttribute("jobRoles", JobRole.values());
        model.addAttribute("statuses", EmploymentStatus.values());
        return "fleet/employments/form";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute Employment employment,
                         BindingResult binding,
                         @RequestParam(value = "file", required = false) MultipartFile file,
                         @RequestParam(value = "type", required = false) DocumentType type,
                         @RequestParam(value = "issueDate", required = false) String issueDate,
                         @RequestParam(value = "expiryDate", required = false) String expiryDate,
                         Model model) throws IOException {

        if (binding.hasErrors()) {
            //model.addAttribute("employees", employeeService.findAll());
        	model.addAttribute("employees", employeeService.findAvailableForEmployment());
            model.addAttribute("docTypes", DocumentType.values());
            model.addAttribute("jobRoles", JobRole.values());
            model.addAttribute("statuses", EmploymentStatus.values());
            return "fleet/employments/form";
        }

        // 1) salva il rapporto
        Employment saved = employmentService.create(employment);

        // 2) allega documento contrattuale se presente
        if (file != null && !file.isEmpty()) {
            LocalDate issued = (issueDate == null || issueDate.isBlank())
                ? null : LocalDate.parse(issueDate);
            LocalDate exp = (expiryDate == null || expiryDate.isBlank())
                ? null : LocalDate.parse(expiryDate);

            documentService.uploadEmploymentDocument(
                saved.getId(), file, type, issued, exp
            );
        }
        //return "redirect:/fleet/employments";
        return "redirect:/fleet/employments/" + saved.getId() + "/edit";
    }
    
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Employment e = employmentService.findByIdOrThrow(id);
        model.addAttribute("employment", e);
        model.addAttribute("employees", employeeService.findAll());
        model.addAttribute("docTypes", DocumentType.values());
        model.addAttribute("documents", documentService.getEmploymentDocuments(id));
        model.addAttribute("jobRoles", JobRole.values());
        model.addAttribute("statuses", EmploymentStatus.values());
        return "fleet/employments/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute Employment employment,
                         BindingResult binding,
                         Model model) {
        if (binding.hasErrors()) {
            model.addAttribute("employees", employeeService.findAll());
            model.addAttribute("docTypes", DocumentType.values());
            model.addAttribute("documents", documentService.getEmploymentDocuments(id));
            model.addAttribute("jobRoles", JobRole.values());
            model.addAttribute("statuses", EmploymentStatus.values());
            return "fleet/employments/form";
        }
        employmentService.update(id, employment);
        return "redirect:/fleet/employments/" + id + "/edit";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        employmentService.delete(id);
        return "redirect:/fleet/employments";
    }
    
    @PostMapping("/{id}/docs")
    public String uploadDoc(@PathVariable Long id,
                            @RequestParam("file") MultipartFile file,
                            @RequestParam("type") DocumentType type,
                            @RequestParam(value="issueDate", required=false) String issueDate,
                            @RequestParam(value="expiryDate", required=false) String expiryDate) throws IOException {
        LocalDate issued = (issueDate == null || issueDate.isBlank()) ? null : LocalDate.parse(issueDate);
        LocalDate exp = (expiryDate == null || expiryDate.isBlank()) ? null : LocalDate.parse(expiryDate);
        documentService.uploadEmploymentDocument(id, file, type, issued, exp);
        return "redirect:/fleet/employments/" + id + "/edit";
    }

    @GetMapping("/{id}/docs/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable Long id,
                                              @PathVariable String filename,
                                              HttpServletRequest request) throws IOException {
        Resource resource = documentService.loadEmploymentDocumentAsResource(id, filename);
        String contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @GetMapping("/{empId}/docs/{docId}/delete")
    public String deleteDocument(@PathVariable Long empId,
                                 @PathVariable Long docId) throws IOException {
        documentService.deleteDocument(docId);
        return "redirect:/fleet/employments/" + empId + "/edit";
    }
    
}
