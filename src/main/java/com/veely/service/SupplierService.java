package com.veely.service;

import com.veely.entity.Supplier;
import com.veely.exception.ResourceNotFoundException;
import com.veely.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SupplierService {

    private final SupplierRepository supplierRepo;

    public Supplier create(Supplier supplier) {
        return supplierRepo.save(supplier);
    }

    public Supplier update(Long id, Supplier payload) {
        Supplier existing = findByIdOrThrow(id);
        existing.setName(payload.getName());
        existing.setVatNumber(payload.getVatNumber());
        existing.setContatto(payload.getContatto());
        existing.setPhone(payload.getPhone());
        existing.setEmail(payload.getEmail());
        existing.setAddress(payload.getAddress());
        return supplierRepo.save(existing);
    }

    @Transactional(readOnly = true)
    public Supplier findByIdOrThrow(Long id) {
        return supplierRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fornitore non trovato: " + id));
    }

    @Transactional(readOnly = true)
    public List<Supplier> findAll() {
        return supplierRepo.findAll();
    }

    public void delete(Long id) {
        Supplier s = findByIdOrThrow(id);
        supplierRepo.delete(s);
    }
}

