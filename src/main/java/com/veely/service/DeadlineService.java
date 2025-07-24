package com.veely.service;

import com.veely.entity.Employment;
import com.veely.entity.Vehicle;
import com.veely.model.DeadlineItem;
import com.veely.repository.EmploymentRepository;
import com.veely.repository.VehicleRepository;
import com.veely.repository.FuelCardRepository;
import com.veely.entity.FuelCard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeadlineService {

    private final VehicleRepository vehicleRepo;
    private final EmploymentRepository employmentRepo;
    private final FuelCardRepository fuelCardRepo;

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
                        "insurance",
                        null,
                        null,
                        null,
                        null));
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
                        "carTax",
                        null,
                        null,
                        null,
                        null));
            }
            FuelCard card = fuelCardRepo.findByVehicleId(v.getId());
            if (card != null && card.getExpiryDate() != null) {
                items.add(new DeadlineItem(
                        "Fuel Card",
                        v.getPlate(),
                        v.getBrand(),
                        v.getModel(),
                        v.getSeries(),
                        card.getExpiryDate(),
                        v.getId(),
                        "fuelCard",
                        null,
                        null,
                        null,
                        null));
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
                        "lease",
                        null,
                        null,
                        null,
                        null));
            }
        }
        items.sort(Comparator.comparing(DeadlineItem::dueDate));
        return items;
    }
    
    /**
     * Returns all deadlines from vehicles and employments combined.
     */
    public List<DeadlineItem> getAllDeadlines() {
        List<DeadlineItem> all = new ArrayList<>();
        all.addAll(getVehicleDeadlines());
        all.addAll(getEmploymentDeadlines());
        all.sort(Comparator.comparing(DeadlineItem::dueDate));
        return all;
    }

    /**
     * Counts deadlines occurring within the given number of days from today.
     *
     * @param days threshold in days
     * @return number of deadlines due within the threshold
     */
    public long countDeadlinesWithinDays(int days) {
        LocalDate now = LocalDate.now();
        return getAllDeadlines().stream()
                .filter(d -> d.dueDate() != null)
                .filter(d -> {
                    long diff = ChronoUnit.DAYS.between(now, d.dueDate());
                    return diff <= days;
                })
                .count();
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
                        "employment",
                        e.getStartDate(),
                        e.getJobTitle(),
                        e.getWorkplace(),
                        e.getJobRole()));
            }
        }
        items.sort(Comparator.comparing(DeadlineItem::dueDate));
        return items;
    }
    
}
