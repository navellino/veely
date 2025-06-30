package com.veely.service;


import com.veely.entity.Assignment;
import com.veely.exception.ResourceNotFoundException;
import com.veely.model.AssignmentStatus;
import com.veely.repository.AssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        return assignmentRepo.save(assignment);
    }

    /** Aggiorna un'assegnazione esistente */
    public Assignment update(Long id, Assignment payload) {
        Assignment existing = findByIdOrThrow(id);
        existing.setEmployment(payload.getEmployment());
        existing.setVehicle(payload.getVehicle());
        existing.setStartDate(payload.getStartDate());
        existing.setEndDate(payload.getEndDate());
        existing.setType(payload.getType());
        existing.setStatus(payload.getStatus());
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
}