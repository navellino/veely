package com.veely.service;

import com.veely.entity.Employment;
import com.veely.entity.Vehicle;
import com.veely.model.DeadlineItem;
import com.veely.repository.EmploymentRepository;
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
    private final EmploymentRepository employmentRepo;

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
                        v.getBrand(),
                        v.getModel(),
                        v.getSeries(),
                        v.getInsuranceExpiryDate(),
                        v.getId(),
                        "insurance"));
            }
            if (v.getCarTaxExpiryDate() != null) {
                items.add(new DeadlineItem(
                        "Bollo",
                        v.getPlate(),
                        v.getBrand(),
                        v.getModel(),
                        v.getSeries(),
                        v.getCarTaxExpiryDate(),
                        v.getId(),
                        "carTax"));
            }
            if (v.getFuelCardExpiryDate() != null) {
                items.add(new DeadlineItem(
                        "Fuel Card",
                        v.getPlate(),
                        v.getBrand(),
                        v.getModel(),
                        v.getSeries(),
                        v.getFuelCardExpiryDate(),
                        v.getId(),
                        "fuelCard"));
            }
            if (v.getContractEndDate() != null) {
                items.add(new DeadlineItem(
                        "Leasing",
                        v.getPlate(),
                        v.getBrand(),
                        v.getModel(),
                        v.getSeries(),
                        v.getContractEndDate(),
                        v.getId(),
                        "lease"));
            }
        }
        items.sort(Comparator.comparing(DeadlineItem::dueDate));
        return items;
    }
    /**
     * Returns a list of deadlines extracted from employments.
     */
    public List<DeadlineItem> getEmploymentDeadlines() {
        List<DeadlineItem> items = new ArrayList<>();
        for (Employment e : employmentRepo.findAll()) {
            if (e.getEndDate() != null) {
                String name = e.getEmployee() != null
                        ? e.getEmployee().getFirstName() + " " + e.getEmployee().getLastName()
                        : e.getMatricola();
                items.add(new DeadlineItem(
                        "Rapporto",
                        name,
                        null,
                        null,
                        null,
                        e.getEndDate(),
                        e.getId(),
                        "employment"));
            }
        }
        items.sort(Comparator.comparing(DeadlineItem::dueDate));
        return items;
    }
}
