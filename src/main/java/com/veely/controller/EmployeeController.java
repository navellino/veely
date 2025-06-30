package com.veely.controller;

import com.veely.entity.Document;
import com.veely.entity.Employee;
import com.veely.entity.Employment;
import com.veely.model.DocumentType;
import com.veely.model.EducationLevel;
import com.veely.model.FullAddress;
import com.veely.model.MaritalStatus;
import com.veely.service.CountryService;
import com.veely.service.CountryService.CountryDto;
import com.veely.service.DocumentService;
import com.veely.service.EmployeeService;
import com.veely.service.EmploymentService;
import com.veely.service.LocationService;
import com.veely.service.LocationService.CityDto;
import com.veely.service.LocationService.ProvinceDto;
import com.veely.service.LocationService.RegionDto;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/fleet/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;
    private final DocumentService documentService;
    private final EmploymentService employmentService;
    private final CountryService countryService;
    private final LocationService locationService; 
    
    @GetMapping("/manage")
    public String manage(
      @RequestParam(defaultValue="0") int page,
      @RequestParam(defaultValue="20") int size,
      Model model
    ) {
    	// 1) Pagine e metadati
        PageRequest pr = PageRequest.of(page, size, Sort.by("lastName"));
        Page<Employee> emps = employeeService.findAll(pr);
        List<Employee> employeeList = emps.getContent();
        
        model.addAttribute("employees", emps);

     // 2) Preparo la mappa con liste vuote
        Map<Long, List<Employment>> empToEmpls = new HashMap<>();
        for (Employee emp : employeeList) {
            empToEmpls.put(emp.getId(), new ArrayList<>());
        }
        
        // 3) Carico tutti gli Employment dei dipendenti correnti
        List<Long> ids = employeeList.stream()
                                     .map(Employee::getId)
                                     .toList();
        List<Employment> allEmpls = employmentService.findByEmployeeIds(ids);
        
     // 4) Popolo la mappa
        for (Employment empl : allEmpls) {
            Long empId = empl.getEmployee().getId();
            empToEmpls.get(empId).add(empl);
        }

        model.addAttribute("empToEmpls", empToEmpls);
        return "fleet/employees/manage";
    }
        
    /** 1) Lista di tutti i dipendenti */
    @GetMapping
    public String list(Model model) {
        model.addAttribute("employees", employeeService.findAll());
        return "fleet/employees/list";
    }

    /** 2) Step 1: form vuoto per creare il dipendente (solo dati anagrafici) */
    
    @GetMapping("/new")
        public String newForm(Model model) {
            // 1) crea un Employee vuoto
            Employee employee = new Employee();
            
            FullAddress ra = employee.getResidenceAddress();
            if (ra == null) {
                ra = new FullAddress();
                employee.setResidenceAddress(ra);
            }
            if (ra.getCountryCode() == null) {
                ra.setCountryCode("IT");
                ra.setCountry("Italia");
            }
            
            // 2) inizializza anche l’address di residenza (altrimenti Thymeleaf fallisce su residenceAddress.*)
            //employee.setResidenceAddress(new FullAddress());
            // 3) metti l’oggetto in model
            model.addAttribute("employee", employee);
            model.addAttribute("residenceAddress", employee.getResidenceAddress());
            // 4) reference data per i dropdown
            model.addAttribute("maritalStatuses", MaritalStatus.values());
            model.addAttribute("educationLevels", EducationLevel.values());
            model.addAttribute("countries", countryService.getAll());
            // carica solo regioni italiane: la select sarà poi popolata via JS da gi_regioni.json
            model.addAttribute("regions", locationService.getRegions("IT"));
            // province e comuni rimangono vuoti: gestiti client-side
            model.addAttribute("provinces", List.of());
            model.addAttribute("cities", List.of());
    
            return "fleet/employees/form";
        }
    
    /**
     * 3) Gestione POST “step 1”: salva l’anagrafica e redirige a /{id}/edit
     *    (cioè riapre lo stesso form in “step 2” con la sezione documenti visibile)
     */
    @PostMapping("/new")
    public String create(
            @Valid @ModelAttribute("employee") Employee employee,
            BindingResult binding,
            Model model
    ) {
        if (binding.hasErrors()) {
            model.addAttribute("docTypes", DocumentType.values());
            return "fleet/employees/form";
        }
        Employee saved = employeeService.create(employee);
        return "redirect:/fleet/employees/" + saved.getId() + "/edit";
    }

    /** 4) Step 2: form di modifica + upload documenti */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        // 1) recupera l’employee esistente
    	Employee employee = employeeService.findByIdOrThrow(id);

    	FullAddress ra = employee.getResidenceAddress();
    	if (ra == null) {
    	    ra = new FullAddress();
    	    employee.setResidenceAddress(ra);
    	}
    	if (ra.getCountryCode() == null) {
    	    ra.setCountryCode("IT");
    	    ra.setCountry("Italia");
    	}
    	
        // 2) se non ha ancora un indirizzo di residenza, creane uno “vuoto”
        if (employee.getResidenceAddress() == null) {
            employee.setResidenceAddress(new FullAddress());
        }
        
        // 3) Carica tutti i documenti
        List<Document> documents = documentService.getEmployeeDocuments(id);
        
        // 3) ora puoi leggere countryCode, stateCode, cityCode senza NPE
        String selectedCountry = employee.getResidenceAddress().getCountryCode();
        String selectedRegion  = employee.getResidenceAddress().getRegionCode();
        String selectedProvince = employee.getResidenceAddress().getProvinceCode();
        String selectedCity     = employee.getResidenceAddress().getCityCode();

        // 4) popoliamo le liste per i drop-down:
        List<CountryDto> countries = countryService.getAll();
        List<RegionDto> regions = locationService.getRegions(selectedCountry);
        // tutte le province del paese selezionato
        List<ProvinceDto> provinces = locationService.getProvinces(selectedCountry);
        // tutte le città della provincia selezionata
        List<CityDto> cities = locationService.getCities(selectedProvince);
        
        model.addAttribute("residenceAddress", employee.getResidenceAddress());
        model.addAttribute("maritalStatuses", MaritalStatus.values());
        model.addAttribute("educationLevels", EducationLevel.values());
        model.addAttribute("countries", countryService.getAll());

        // 5) aggiungi tutto al Model
        model.addAttribute("employee", employee);
        
        String cc = employee.getResidenceAddress() != null ? employee.getResidenceAddress().getCountryCode() : null;
        if (cc != null) {
          model.addAttribute("regions", locationService.getRegions(cc));
          String rc = employee.getResidenceAddress().getRegionCode();
          if (rc != null) {
            model.addAttribute("provinces", locationService.getProvinces(rc));
            String pc = employee.getResidenceAddress().getProvinceCode();
            if (pc != null) {
              model.addAttribute("cities", locationService.getCities(pc));
            }
          }
        }
        
        //Ricava la foto profilo se esiste
        Document profilePhoto = documents.stream()
                .filter(doc -> doc.getType() == DocumentType.IDENTITY_PHOTO)
                .findFirst()
                .orElse(null);
        
        model.addAttribute("profilePhoto", profilePhoto);
        model.addAttribute("documents", documents);
        model.addAttribute("docTypes", DocumentType.values());

        return "fleet/employees/form";
    }
    
    @PostMapping("/{id}/docs")
    public String uploadDoc(
        @PathVariable Long id,
        @RequestParam("file") MultipartFile file,
        @RequestParam("type") DocumentType type,
        @RequestParam(value="issueDate", required=false) String issueDate,
        @RequestParam(value="expiryDate", required=false) String expiryDate,
        RedirectAttributes ra
    ) throws IOException {
        LocalDate issued = (issueDate == null || issueDate.isBlank())
            ? null : LocalDate.parse(issueDate);
        LocalDate exp = (expiryDate == null || expiryDate.isBlank())
            ? null : LocalDate.parse(expiryDate);
        documentService.uploadEmployeeDocument(id, file, type, issued, exp);
        ra.addFlashAttribute("success", "Documento caricato");
        return "redirect:/fleet/employees/" + id + "/edit";
    }

    /**
     * 5) Gestione POST “step 2”: aggiorna anagrafica, salva eventuale file,
     *    e rimane sul medesimo form (per caricare altri documenti o uscire).
     */
    
    @PostMapping("/{id}/edit")
    public String update(
        @PathVariable Long id,
        @Valid @ModelAttribute("employee") Employee employee,
        BindingResult binding,
        Model model
    ) {
        if (binding.hasErrors()) {
            // ripopolo le select in caso di validazione KO
            model.addAttribute("maritalStatuses", MaritalStatus.values());
            model.addAttribute("educationLevels", EducationLevel.values());
            model.addAttribute("countries", countryService.getAll());
            String cc = employee.getResidenceAddress().getCountryCode();
            String rc = employee.getResidenceAddress().getRegionCode();
            String sc = employee.getResidenceAddress().getStreet();
            model.addAttribute("regions", locationService.getRegions(cc));
            model.addAttribute("provinces", locationService.getProvinces(rc));
            model.addAttribute("street", locationService.getStreet(sc));
            model.addAttribute("cities", locationService.getCities(employee.getResidenceAddress().getProvinceCode()));
            model.addAttribute("docTypes", DocumentType.values());
            model.addAttribute("documents", documentService.getEmployeeDocuments(id));
            return "fleet/employees/form";
        }
        employee.setId(id);
        employeeService.update(id, employee);
        return "redirect:/fleet/employees/" + id + "/edit";
    }

    /**
     * 6) Download di un documento personale:
     *    GET /fleet/employees/{id}/docs/{filename}
     */
    @GetMapping("/{id}/docs/{filename:.+}")
    public ResponseEntity<Resource> serveFile(
            @PathVariable Long id,
            @PathVariable String filename,
            HttpServletRequest request
    ) throws IOException {
       // Resource resource = documentService.loadEmployeeDocumentAsResource(id, "employees/" + id + "/docs/" + filename);
    	Resource resource = documentService.loadEmployeeDocumentAsResource(id, filename);
    	
        String contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
    
    @GetMapping("/{empId}/docs/{docId}/delete")
    public String deleteEmployeeDocument(
            @PathVariable Long empId,
            @PathVariable Long docId,
            RedirectAttributes redirectAttrs) {

        documentService.deleteEmployeeDocument(empId, docId);
        redirectAttrs.addFlashAttribute("success", "Documento eliminato");
        return "redirect:/fleet/employees/" + empId + "/edit";
    }
    
    private void prepareReferenceData(Model model) {
        model.addAttribute("maritalStatuses", employeeService.listMaritalStatuses());
        model.addAttribute("educationLevels", employeeService.listEducationLevels());
        model.addAttribute("countries", countryService.getAll());
      }
}