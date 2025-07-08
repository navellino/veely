package com.veely.controller;

import com.veely.entity.Assignment;
import com.veely.entity.Document;
import com.veely.entity.Vehicle;
import com.veely.model.DocumentType;
import com.veely.model.OwnershipType;
import com.veely.model.VehicleStatus;
import com.veely.repository.DocumentRepository;
import com.veely.service.AssignmentService;
import com.veely.service.DocumentService;
import com.veely.service.SupplierService;
import com.veely.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/fleet/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;
    private final DocumentService documentService;
    private final DocumentRepository documentRepo; // per eventuali operazioni dirette
    private final SupplierService supplierService;
    private final AssignmentService assignmentService;

    /** Mostra la lista (tabella) di tutti i veicoli **/
    @GetMapping
    public String list(Model model) {
        List<Vehicle> list = vehicleService.findAll();
        model.addAttribute("vehicles", list);
        return "fleet/vehicles/index";
    }

    /** Form di creazione veicolo */
    @GetMapping("/new")
    public String createForm(Model model) {
    	model.addAttribute("vehicle", new Vehicle());
        addFormOptions(model);
        return "fleet/vehicles/form";
    }

    /** Salva un nuovo veicolo */
    @PostMapping("new")
    public String saveNew(@Valid @ModelAttribute("vehicle") Vehicle vehicle,
                          BindingResult binding, Model model) {
        if (binding.hasErrors()) {
        	addFormOptions(model);
            return "fleet/vehicles/form";
        }
        Vehicle saved = vehicleService.create(vehicle);
        return "redirect:/fleet/vehicles/" + saved.getId() + "/edit";
    }

    /** Form di modifica veicolo con sezione documenti */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Vehicle v = vehicleService.findByIdOrThrow(id);
        List<Document> docs = documentService.getVehicleDocuments(id);
        Document image = docs.stream()
                .filter(d -> d.getType() == DocumentType.VEHICLE_IMAGE)
                .findFirst()
                .orElse(null);
        
        model.addAttribute("vehicle", v);
        model.addAttribute("documents", docs);
        model.addAttribute("vehicleImage", image);
        addFormOptions(model);
        return "fleet/vehicles/form";
    }

    /** Aggiorna un veicolo esistente */
    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("vehicle") Vehicle vehicle,
                         BindingResult binding, Model model) {
        if (binding.hasErrors()) {
            List<Document> docs = documentService.getVehicleDocuments(id);
            Document image = docs.stream()
                    .filter(d -> d.getType() == DocumentType.VEHICLE_IMAGE)
                    .findFirst()
                    .orElse(null);
            model.addAttribute("documents", docs);
            model.addAttribute("vehicleImage", image);
            addFormOptions(model);
            return "fleet/vehicles/form";
        }
        vehicleService.update(id, vehicle);
        return "redirect:/fleet/vehicles/" + id + "/edit";
    }
    
    /** Aggiorna il chilometraggio corrente del veicolo */
    @PostMapping("/{id}/mileage")
    public String updateMileage(@PathVariable Long id,
                                @RequestParam("newMileage") int newMileage) {
        vehicleService.updateMileage(id, newMileage);
        return "redirect:/fleet/vehicles/" + id;
    }

    /** Elimina un veicolo */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        vehicleService.delete(id);
        return "redirect:/fleet/vehicles";
    }

    /** Mostra il dettaglio di un veicolo, con tab per foto e documenti */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Vehicle v = vehicleService.findByIdOrThrow(id);
        List<Document> docs = documentRepo.findByVehicleId(id);
        Document image = docs.stream()
                .filter(d -> d.getType() == DocumentType.VEHICLE_IMAGE)
                .findFirst()
                .orElse(null);
        Assignment asg = assignmentService.findActiveByVehicle(id);
        if (asg != null) {
            model.addAttribute("assignedEmployee", asg.getEmployment().getEmployee());
        }
        model.addAttribute("vehicle", v);
        model.addAttribute("documents", docs);
        model.addAttribute("vehicleImage", image);
        model.addAttribute("docTypes", DocumentType.values());
        return "fleet/vehicles/detail";
    }

    /** Upload foto veicolo */
    @PostMapping("/{id}/photos")
    public String uploadPhoto(@PathVariable Long id,
                              @RequestParam("file") MultipartFile file) throws IOException {
        vehicleService.uploadPhoto(id, file);
        return "redirect:/fleet/vehicles/" + id + "/edit";
    }

    /** Upload documento veicolo */
    @PostMapping("/{id}/docs")
    public String uploadDoc(@PathVariable Long id,
                            @RequestParam("file") MultipartFile file,
                            @RequestParam("type") DocumentType type,
                            @RequestParam("issueDate") String issueDate,
                            @RequestParam("expiryDate") String expiryDate) {
        LocalDate issued = issueDate.isBlank() ? null : LocalDate.parse(issueDate);
        LocalDate exp = expiryDate.isBlank() ? null : LocalDate.parse(expiryDate);
        vehicleService.uploadDocument(id, file, type, issued, exp);
        return "redirect:/fleet/vehicles/" + id + "/edit";
    }
    
    /** Upload documento veicolo */
    @PostMapping("/{id}/det")
    public String uploadDocDet(@PathVariable Long id,
                            @RequestParam("file") MultipartFile file,
                            @RequestParam("type") DocumentType type,
                            @RequestParam("issueDate") String issueDate,
                            @RequestParam("expiryDate") String expiryDate) {
        LocalDate issued = issueDate.isBlank() ? null : LocalDate.parse(issueDate);
        LocalDate exp = expiryDate.isBlank() ? null : LocalDate.parse(expiryDate);
        vehicleService.uploadDocument(id, file, type, issued, exp);
        return "redirect:/fleet/vehicles/" + id;
    }
    
    /** Download di un file (foto o doc): restituirà il Resource e header content disposition */
    @GetMapping("/files/{area}/{filename:.+}")
    @ResponseBody
    public Resource serveFile(@PathVariable String area,
                              @PathVariable String filename) {
        return vehicleService.loadDocument(Long.valueOf(area), filename);
        // Si assume area = vehicleId, per foto cambiare loadPhoto se necessario.
    }
    
    /** Elimina un documento veicolo */
    @GetMapping("/{vehId}/docs/{docId}/delete")
    public String deleteDocument(@PathVariable("vehId") Long vehId,
                                 @PathVariable("docId") Long docId) throws IOException {
        documentService.deleteDocument(docId);
        return "redirect:/fleet/vehicles/" + vehId;
    }
    
    private void addFormOptions(Model model) {
        model.addAttribute("statuses", VehicleStatus.values());
        model.addAttribute("docTypes", DocumentType.values());
        model.addAttribute("ownershipTypes", OwnershipType.values());
        model.addAttribute("suppliers", supplierService.findAll());
    }
}
