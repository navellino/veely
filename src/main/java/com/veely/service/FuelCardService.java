package com.veely.service;

import com.veely.entity.FuelCard;
import com.veely.exception.ResourceNotFoundException;
import com.veely.repository.FuelCardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FuelCardService {

    private final FuelCardRepository fuelCardRepo;

    public FuelCard create(FuelCard card) {
        return fuelCardRepo.save(card);
    }

    public FuelCard update(Long id, FuelCard payload) {
        FuelCard existing = findByIdOrThrow(id);
        existing.setCardNumber(payload.getCardNumber());
        existing.setExpiryDate(payload.getExpiryDate());
        existing.setSupplier(payload.getSupplier());
        existing.setEmployee(payload.getEmployee());
        existing.setVehicle(payload.getVehicle());
        existing.setPlafond(payload.getPlafond());
        return existing;
    }

    @Transactional(readOnly = true)
    public FuelCard findByIdOrThrow(Long id) {
        return fuelCardRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Fuel card non trovata: " + id));
    }

    @Transactional(readOnly = true)
    public List<FuelCard> findAll() {
        return fuelCardRepo.findAll();
    }

    public void delete(Long id) {
        FuelCard card = findByIdOrThrow(id);
        fuelCardRepo.delete(card);
    }
}
