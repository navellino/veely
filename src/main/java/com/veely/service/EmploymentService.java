package com.veely.service;

import com.veely.entity.Employment;
import com.veely.entity.Document;
import com.veely.exception.ResourceNotFoundException;
import com.veely.model.EmploymentStatus;
import com.veely.repository.DocumentRepository;
import com.veely.repository.EmployeeRepository;
import com.veely.repository.EmploymentRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * Servizio per la gestione CRUD di Employment,
 * ricerca/filtro/paginazione e pulizia file/documenti.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class EmploymentService {

    private final EmploymentRepository employmentRepo;
    private final DocumentRepository documentRepo;
    private final FileSystemStorageService fileStorage;
    private final EmployeeService employeeService;
    private final EmployeeRepository employeeRepo;

    /**
     * Crea un nuovo rapporto di lavoro.
     */
    /* ---------- CREATE ---------- */
    public Employment create(Employment employment) {
        // (A)  Aggancia il dipendente se viene passato solo l’id
        if (employment.getEmployee() != null &&
            employment.getEmployee().getId() != null) {
            employment.setEmployee(employeeRepo.findById(employment.getEmployee().getId()).get());
        }

        /* (B)  EmploymentAddress è già popolato dal binding
                – nessun merge manuale necessario */
        //return employmentRepo.save(employment);
        Employment saved = employmentRepo.save(employment);
        // crea la cartella per i documenti usando la matricola
        fileStorage.initDirectory("employments/" + saved.getMatricola() + "/docs");
        return saved;
    }

    /**
     * Aggiorna un rapporto di lavoro esistente.
     */
    /* ---------- UPDATE ---------- */
    public Employment update(Long id, Employment payload) {

        Employment existing = employmentRepo.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Employment " + id + " not found"));

        /* campi “semplici” */
        existing.setStartDate(payload.getStartDate());
        existing.setEndDate(payload.getEndDate());
        existing.setJobDescription(payload.getJobDescription());
        existing.setSalary(payload.getSalary());
        existing.setStatus(payload.getStatus());

        /* (C)  Sostituisci l’intero embeddable                             
                → se preferisci aggiornare campo-per-campo fai i setter singoli */
        existing.setWorkplace(payload.getWorkplace());

        return existing;
    }

    /**
     * Trova un Employment per ID o lancia eccezione se non esiste.
     */
    @Transactional(readOnly = true)
    public Employment findByIdOrThrow(Long id) {
        return employmentRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rapporto di lavoro non trovato: " + id));
    }

    /**
     * Elenca tutti i rapporti di lavoro.
     */
    @Transactional(readOnly = true)
    public List<Employment> findAll() {
        return employmentRepo.findAll();
    }

    /**
     * Elenca tutti i rapporti di lavoro con paginazione.
     */
    @Transactional(readOnly = true)
    public Page<Employment> findAll(Pageable pageable) {
        return employmentRepo.findAll(pageable);
    }

    /**
     * Filtra per stato del rapporto di lavoro (ACTIVE, TERMINATED, etc) con paginazione.
     */
    @Transactional(readOnly = true)
    public Page<Employment> findByStatus(EmploymentStatus status, Pageable pageable) {
        return employmentRepo.findByStatus(status, pageable);
    }

    /**
     * Ricerca per titolo di lavoro contenente keyword, paginata.
     */
    @Transactional(readOnly = true)
    public Page<Employment> searchByJobTitle(String keyword, Pageable pageable) {
        String like = "%" + keyword.trim().toLowerCase() + "%";
        return employmentRepo.findByJobTitleIgnoreCaseContaining(like, pageable);
    }

    /**
     * Elimina un rapporto di lavoro e i suoi documenti (DB + filesystem).
     */
    public void delete(Long id) {
        Employment e = findByIdOrThrow(id);
        // Elimina documenti contrattuali associati
        List<Document> docs = documentRepo.findByEmploymentId(id);
        docs.forEach(doc -> {
            String fullPath = doc.getPath();
            int sep = fullPath.lastIndexOf('/');
            String subDir = sep > 0 ? fullPath.substring(0, sep) : "";
            String filename = sep > 0 ? fullPath.substring(sep + 1) : fullPath;
            fileStorage.delete(filename, subDir);
        });
        documentRepo.deleteAll(docs);
        // Rimuove directory fisica
        //fileStorage.deleteDirectory("employments/" + id + "/docs");
        fileStorage.deleteDirectory("employments/" + e.getMatricola() + "/docs");
        // Cancella il rapporto di lavoro
        employmentRepo.delete(e);
    }
    
    @Transactional(readOnly = true)
    public List<Employment> findByEmployeeId(Long employeeId) {
        return employmentRepo.findByEmployeeId(employeeId);
    }
    
    /**
     * Recupera tutte le Employment per i dipendenti indicati e le raggruppa per employeeId.
     */
    public List<Employment> findByEmployeeIds(List<Long> ids) {
        if (ids.isEmpty()) return List.of();
        return employmentRepo.findByEmployeeIdIn(ids);
    }
    
}
