package com.veely.service;

import com.veely.entity.DocumentCategory;
import com.veely.entity.DocumentTypeEntity;
import com.veely.repository.DocumentTypeEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentTypeService {
    private final DocumentTypeEntityRepository repository;

    public List<DocumentTypeEntity> findAll() {
        return repository.findAll();
    }

    public List<DocumentTypeEntity> findByCategory(DocumentCategory category) {
        return repository.findByCategory(category);
    }

    public DocumentTypeEntity findByCode(String code) {
        return repository.findByCode(code);
    }
}
