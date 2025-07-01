package com.veely.service;

import com.veely.entity.Assignment;
import com.veely.entity.Document;
import com.veely.entity.Employee;
import com.veely.entity.Employment;
import com.veely.entity.Vehicle;
import com.veely.exception.ResourceNotFoundException;
import com.veely.model.DocumentType;
import com.veely.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DocumentService {

    private final DocumentRepository documentRepo;
    private final FileSystemStorageService fileStorage;
    private final EmployeeService employeeService;
    private final EmploymentService employmentService;
    private final VehicleService vehicleService;
    private final AssignmentService assignmentService;


    /** Recupera un documento o lancia eccezione */
    @Transactional(readOnly = true)
    public Document getDocument(Long documentId) {
        return documentRepo.findById(documentId)
            .orElseThrow(() -> new ResourceNotFoundException("Documento non trovato: " + documentId));
    }

    /** Elimina file e record DB */
    public void deleteDocument(Long documentId) throws IOException {
        Document doc = documentRepo.findById(documentId)
            .orElseThrow(() -> new ResourceNotFoundException("Documento non trovato: " + documentId));
        Path p = Path.of(doc.getPath());
        String subdir = p.getParent().toString();
        String fn    = p.getFileName().toString();
        fileStorage.delete(fn, subdir);
        documentRepo.delete(doc);
    }

    /** Carica risorsa per download */
    @Transactional(readOnly = true)
    public Resource loadDocument(Long documentId) {
        Document doc = getDocument(documentId);
        Path p = Path.of(doc.getPath());
        String subdir = p.getParent().toString();
        String filename = p.getFileName().toString();
        return fileStorage.loadAsResource(filename, subdir);
    }

    /** Restituisce tutti i documenti di un dipendente */
    @Transactional(readOnly = true)
    public List<Document> getEmployeeDocuments(Long employeeId) {
        // derivato da Document.employee.id
        return documentRepo.findByEmployeeId(employeeId);
    }

    // --- Employment Documents ---
    @Transactional(readOnly = true)
    public List<Document> getEmploymentDocuments(Long employmentId) {
        return documentRepo.findByEmploymentId(employmentId);
    }

    public Document uploadEmploymentDocument(Long employmentId,
                                             MultipartFile file,
                                             DocumentType type,
                                             LocalDate issueDate,
                                             LocalDate expiryDate) throws IOException {
        Employment empmt = employmentService.findByIdOrThrow(employmentId);
        //String subdir = "employments/" + employmentId + "/docs";
        String subdir = "employments/" + empmt.getMatricola() + "/docs";
        fileStorage.initDirectory(subdir);
        String filename = fileStorage.store(file, subdir);

        Document doc = Document.builder()
            .employment(empmt)
            .type(type)
            .issueDate(issueDate)
            .expiryDate(expiryDate)
            .path(subdir + "/" + filename)
            .build();
        return documentRepo.save(doc);
    }

    // --- Vehicle Documents ---
    @Transactional(readOnly = true)
    public List<Document> getVehicleDocuments(Long vehicleId) {
        return documentRepo.findByVehicleId(vehicleId);
    }

    public Document uploadVehicleDocument(Long vehicleId,
                                          MultipartFile file,
                                          DocumentType type,
                                          LocalDate issueDate,
                                          LocalDate expiryDate) throws IOException {
        Vehicle veh = vehicleService.findByIdOrThrow(vehicleId);
        String subdir = "vehicles/" + vehicleId + "/docs";
        fileStorage.initDirectory(subdir);
        String filename = fileStorage.store(file, subdir);

        Document doc = Document.builder()
            .vehicle(veh)
            .type(type)
            .issueDate(issueDate)
            .expiryDate(expiryDate)
            .path(subdir + "/" + filename)
            .build();
        return documentRepo.save(doc);
    }

    // --- Assignment Documents ---
    @Transactional(readOnly = true)
    public List<Document> getAssignmentDocuments(Long assignmentId) {
        return documentRepo.findByAssignmentId(assignmentId);
    }

    public Document uploadAssignmentDocument(Long assignmentId,
                                             MultipartFile file,
                                             DocumentType type,
                                             LocalDate issueDate,
                                             LocalDate expiryDate) throws IOException {
        Assignment asg = assignmentService.findByIdOrThrow(assignmentId);
        String subdir = "assignments/" + assignmentId + "/docs";
        fileStorage.initDirectory(subdir);
        String filename = fileStorage.store(file, subdir);

        Document doc = Document.builder()
            .assignment(asg)
            .type(type)
            .issueDate(issueDate)
            .expiryDate(expiryDate)
            .path(subdir + "/" + filename)
            .build();
        return documentRepo.save(doc);
    }
    
    /**
     * Carica e salva un documento per un dipendente.
     */
    /** Carica fisicamente e salva in DB un documento per un dipendente */
    public Document uploadEmployeeDocument(Long employeeId,
                                           MultipartFile file,
                                           DocumentType type,
                                           LocalDate issueDate,
                                           LocalDate expiryDate) throws IOException {
        // 1. Recupera l’employee o lancia eccezione
        Employee emp = employeeService.findByIdOrThrow(employeeId);
        // 2. Prepara la directory e salva il file
        String dir = "employees/" + employeeId + "/docs";
        fileStorage.initDirectory(dir);
        String filename = fileStorage.store(file, dir);
        // 3. Costruisci il Document con il builder corretto
        Document doc = Document.builder()
            .employee(emp)
            .type(type)
            .path(dir + "/" + filename)
            .issueDate(issueDate)
            .expiryDate(expiryDate)
            .build();
        return documentRepo.save(doc);
    }

    /** Carica la risorsa Spring Resource per il download di un documento dipendente */
    @Transactional(readOnly = true)
    public Resource loadEmployeeDocumentAsResource(Long employeeId, String filename) {
        String dir = "employees/" + employeeId + "/docs";
        return fileStorage.loadAsResource(filename, dir);
    }
    
    /** Carica la risorsa per un documento di rapporto di lavoro */
    @Transactional(readOnly = true)
    public Resource loadEmploymentDocumentAsResource(Long employmentId, String filename) {
        Employment emp = employmentService.findByIdOrThrow(employmentId);
        String dir = "employments/" + emp.getMatricola() + "/docs";
        return fileStorage.loadAsResource(filename, dir);
    }
    
    @Transactional
    public void deleteEmployeeDocument(Long empId, Long docId) {
        Document doc = documentRepo.findById(docId).get();
        // cancello il file dal file system
        fileStorage.delete(doc.getPath());
        // cancello il record
        documentRepo.delete(doc);
    }
    
    
}
