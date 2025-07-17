package com.veely.service;

import com.veely.entity.Project;
import com.veely.exception.ResourceNotFoundException;
import com.veely.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectService {
    private final ProjectRepository projectRepo;

    public Project create(Project project) {
        return projectRepo.save(project);
    }

    public Project update(Long id, Project payload) {
        Project existing = findByIdOrThrow(id);
        existing.setName(payload.getName());
        return projectRepo.save(existing);
    }

    @Transactional(readOnly = true)
    public Project findByIdOrThrow(Long id) {
        return projectRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commessa non trovata: " + id));
    }

    @Transactional(readOnly = true)
    public List<Project> findAll() {
        return projectRepo.findAll();
    }

    public void delete(Long id) {
        Project p = findByIdOrThrow(id);
        projectRepo.delete(p);
    }
}
