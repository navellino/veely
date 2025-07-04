package com.veely.service;


import com.veely.entity.Assignment;
import com.veely.entity.Vehicle;
import com.veely.exception.ResourceNotFoundException;
import com.veely.model.AssignmentStatus;
import com.veely.model.AssignmentType;
import com.veely.model.VehicleStatus;
import com.veely.repository.AssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AssignmentService {

    private final AssignmentRepository assignmentRepo;

    /** Crea una nuova assegnazione (status default ACTIVE) */
    public Assignment create(Assignment assignment) {
        if (assignment.getStatus() == null) {
            assignment.setStatus(AssignmentStatus.ASSIGNED);
        }
        updateVehicleStatus(assignment);
        return assignmentRepo.save(assignment);
    }

    /** Aggiorna un'assegnazione esistente */
    public Assignment update(Long id, Assignment payload) {
        Assignment existing = findByIdOrThrow(id);
        Vehicle previousVehicle = existing.getVehicle();
        
        existing.setEmployment(payload.getEmployment());
        existing.setVehicle(payload.getVehicle());
        existing.setStartDate(payload.getStartDate());
        existing.setEndDate(payload.getEndDate());
        existing.setType(payload.getType());
        existing.setStatus(payload.getStatus());
        
        if (previousVehicle != null &&
                (payload.getVehicle() == null ||
                 !previousVehicle.getId().equals(payload.getVehicle().getId()))) {
                previousVehicle.setStatus(VehicleStatus.IN_SERVICE);
            }
        
        updateVehicleStatus(existing);
        return existing;
    }

    /** Trova per ID o lancia ResourceNotFoundException */
    @Transactional(readOnly = true)
    public Assignment findByIdOrThrow(Long id) {
        return assignmentRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Assegnazione non trovata: " + id));
    }

    /** Lista tutte le assegnazioni */
    @Transactional(readOnly = true)
    public List<Assignment> findAll() {
        return assignmentRepo.findAll();
    }

    /** Lista paginata */
    @Transactional(readOnly = true)
    public Page<Assignment> findAll(Pageable pageable) {
        return assignmentRepo.findAll(pageable);
    }

    /** Filtra per stato con paginazione */
    @Transactional(readOnly = true)
    public Page<Assignment> findByStatus(AssignmentStatus status, Pageable pageable) {
        return assignmentRepo.findByStatus(status, pageable);
    }

    /** Elimina un'assegnazione */
    public void delete(Long id) {
        Assignment a = findByIdOrThrow(id);
        assignmentRepo.delete(a);
    }
    
    /** Restituisce le assegnazioni filtrate per tipologia. */
    @Transactional(readOnly = true)
    public List<Assignment> findByType(AssignmentType type) {
        return assignmentRepo.findByTypeOrderByStartDateDesc(type);
    }
    
    /**
     * Aggiorna lo stato di tutte le assegnazioni scadute e libera i veicoli.
     */
    public void releaseExpiredAssignments() {
        LocalDate today = LocalDate.now();
        List<Assignment> expired = assignmentRepo.findByStatusAndEndDateBefore(AssignmentStatus.ASSIGNED, today);
        for (Assignment a : expired) {
            a.setStatus(AssignmentStatus.RETURNED);
            updateVehicleStatus(a);
        }
    }

    /** Aggiorna lo stato del veicolo in base all'assegnazione. */
    private void updateVehicleStatus(Assignment assignment) {
        Vehicle v = assignment.getVehicle();
        if (v == null) return;
        LocalDate end = assignment.getEndDate();
        boolean active = assignment.getStatus() == AssignmentStatus.ASSIGNED &&
                (end == null || !end.isBefore(LocalDate.now()));
        v.setStatus(active ? VehicleStatus.ASSIGNED : VehicleStatus.IN_SERVICE);
    }
}