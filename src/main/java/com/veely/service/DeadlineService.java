package com.veely.service;

import com.veely.entity.Vehicle;
import com.veely.model.DeadlineItem;
import com.veely.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeadlineService {

    private final VehicleRepository vehicleRepo;

    /**
     * Returns a list of deadlines extracted from vehicles.
     */
    public List<DeadlineItem> getVehicleDeadlines() {
        List<DeadlineItem> items = new ArrayList<>();
        for (Vehicle v : vehicleRepo.findAll()) {
            if (v.getInsuranceExpiryDate() != null) {
                items.add(new DeadlineItem(
                        "Assicurazione",
                        v.getPlate(),
                        v.getInsuranceExpiryDate(),
                        v.getId(),
                        "insurance"));
            }
            if (v.getCarTaxExpiryDate() != null) {
                items.add(new DeadlineItem(
                        "Bollo",
                        v.getPlate(),
                        v.getCarTaxExpiryDate(),
                        v.getId(),
                        "carTax"));
            }
            if (v.getFuelCardExpiryDate() != null) {
                items.add(new DeadlineItem(
                        "Fuel Card",
                        v.getPlate(),
                        v.getFuelCardExpiryDate(),
                        v.getId(),
                        "fuelCard"));
            }
        }
        items.sort(Comparator.comparing(DeadlineItem::dueDate));
        return items;
    }
}
