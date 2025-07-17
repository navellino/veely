package com.veely.service;

import com.veely.entity.Employee;
import com.veely.entity.Document;
import com.veely.exception.ResourceNotFoundException;
import com.veely.model.EducationLevel;
import com.veely.model.EmploymentStatus;
import com.veely.model.MaritalStatus;
import com.veely.model.UserRole;
import com.veely.repository.DocumentRepository;
import com.veely.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Servizio per la gestione CRUD di Employee, ricerca/filtro/paginazione,
 * e pulizia dei documenti personali dal DB e filesystem.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepo;
    private final DocumentRepository documentRepo;
    private final FileSystemStorageService fileStorage;
    private final PasswordEncoder passwordEncoder;

    /**
     * Crea un nuovo dipendente. Assegna ruolo USER se mancante.
     */
    public Employee create(Employee employee) {
        if (employee.getRole() == null) {
            employee.setRole(UserRole.USER);
        }
        if (StringUtils.hasText(employee.getPassword())) {
            employee.setPassword(passwordEncoder.encode(employee.getPassword()));
        }
        return employeeRepo.save(employee);
    }
    
 // restituisce tutti i valori dell’enum, ordinati per displayName
    public List<MaritalStatus> listMaritalStatuses() {
      return Arrays.stream(MaritalStatus.values())
                   .sorted(Comparator.comparing(MaritalStatus::getDisplayName))
                   .toList();
    }

    public List<EducationLevel> listEducationLevels() {
      return Arrays.stream(EducationLevel.values())
                   .sorted(Comparator.comparing(EducationLevel::getDisplayName))
                   .toList();
    }

    @Transactional
    public Employee update(Long id, Employee payload) {
        // 1) carico l’esistente
        Employee existing = findByIdOrThrow(id);

        // 2) sovrascrivo solo i campi anagrafici
        existing.setFirstName(payload.getFirstName());
        existing.setLastName(payload.getLastName());
        existing.setBirthDate(payload.getBirthDate());
        existing.setBirthPlace(payload.getBirthPlace());
        existing.setGender(payload.getGender());
        existing.setFiscalCode(payload.getFiscalCode());
        existing.setIban(payload.getIban());
        
     // 2) Contatti e ruolo
        existing.setEmail(payload.getEmail());
        existing.setPhone(payload.getPhone());
        existing.setMobile(payload.getMobile());
        existing.setPec(payload.getPec());
        existing.setRole(payload.getRole());
        existing.setEmployeeRole(payload.getEmployeeRole());

        // 3) password: se ne è stata fornita una nuova, la codifico e la setto
        String newPwd = payload.getPassword();
        if (newPwd != null && !newPwd.isBlank()) {
            existing.setPassword(passwordEncoder.encode(newPwd));
        }
        // altrimenti lasciamo inalterata quella già in DB
        
     // 3) Stati civili e titoli di studio
        existing.setMaritalStatus(payload.getMaritalStatus());
        existing.setEducationLevel(payload.getEducationLevel());
        
     // 4) Indirizzo di residenza (embed)
        existing.setResidenceAddress(payload.getResidenceAddress());

        // 4) salvo e rendo persistente
        return employeeRepo.save(existing);
    }

    /**
     * Restituisce il dipendente per ID o lancia eccezione se non esiste.
     */
    @Transactional(readOnly = true)
    public Employee findByIdOrThrow(Long id) {
        return employeeRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dipendente non trovato: " + id));
    }

    /**
     * Elenca tutti i dipendenti (senza paginazione).
     */
    @Transactional(readOnly = true)
    public List<Employee> findAll() {
        return employeeRepo.findAll();
    }

    /** Restituisce i dipendenti senza un rapporto di lavoro attivo */
    @Transactional(readOnly = true)
    public List<Employee> findAvailableForEmployment() {
        return employeeRepo.findAvailableForEmployment(EmploymentStatus.ACTIVE);
    }
    
    /**
     * Elenca i dipendenti con paginazione.
     */
    @Transactional(readOnly = true)
    public Page<Employee> findAll(Pageable pageable) {
        return employeeRepo.findAll(pageable);
    }

    /**
     * Filtra per ruolo utente con paginazione.
     */
    @Transactional(readOnly = true)
    public Page<Employee> findByRole(UserRole role, Pageable pageable) {
        return employeeRepo.findByRole(role, pageable);
    }

    /**
     * Ricerca per nome o cognome contenente la parola chiave, paginata.
     */
    @Transactional(readOnly = true)
    public Page<Employee> search(String keyword, Pageable pageable) {
        String like = "%" + keyword.trim().toLowerCase() + "%";
        return employeeRepo.findByFirstNameIgnoreCaseContainingOrLastNameIgnoreCaseContaining(like, like, pageable);
    }

    /**
     * Elimina un dipendente e pulisce i suoi documenti personali dal DB e dal filesystem.
     */
    public void delete(Long id) {
        Employee e = findByIdOrThrow(id);
        // Rimuovo record Document associati all'employee
        List<Document> docs = documentRepo.findByEmployeeId(id);
        docs.forEach(doc -> {
            String fullPath = doc.getPath();
            int sep = fullPath.lastIndexOf('/');
            String subDir = sep > 0 ? fullPath.substring(0, sep) : "";
            String filename = sep > 0 ? fullPath.substring(sep + 1) : fullPath;
            fileStorage.delete(filename, subDir);
        });
        documentRepo.deleteAll(docs);
        // Rimuovo directory fisica
        fileStorage.deleteDirectory("employees/" + id + "/docs");
        // Cancello l'entity
        employeeRepo.delete(e);
    }
    
    public Resource loadEmployeeDocumentAsResource(Long employeeId, String filename) {
        // es. delega a FileSystemStorageService
        return fileStorage.loadAsResource("employee/" + employeeId + "/docs/", filename);
    }
        
}