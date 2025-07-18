package com.veely.service;

import com.veely.entity.EmployeeRole;
import com.veely.exception.ResourceNotFoundException;
import com.veely.repository.EmployeeRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeRoleService {

    private final EmployeeRoleRepository roleRepo;

    public EmployeeRole create(EmployeeRole role) {
        return roleRepo.save(role);
    }

    public EmployeeRole update(Long id, EmployeeRole payload) {
        EmployeeRole existing = findByIdOrThrow(id);
        existing.setName(payload.getName());
        return roleRepo.save(existing);
    }

    @Transactional(readOnly = true)
    public EmployeeRole findByIdOrThrow(Long id) {
        return roleRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ruolo non trovato: " + id));
    }

    @Transactional(readOnly = true)
    public List<EmployeeRole> findAll() {
        return roleRepo.findAll();
    }

    public void delete(Long id) {
        EmployeeRole r = findByIdOrThrow(id);
        roleRepo.delete(r);
    }
}
