package com.veely.service;

import com.veely.model.AssignmentStatus;
import com.veely.model.VehicleStatus;
import com.veely.repository.AssignmentRepository;
import com.veely.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final VehicleRepository vehicleRepo;
    private final AssignmentRepository assignmentRepo;

    public DashboardMetrics getMetrics() {
        long vehicles = vehicleRepo.count();
        long inService = vehicleRepo.countByStatus(VehicleStatus.IN_SERVICE);
        long assigned = assignmentRepo.countDistinctVehicleByStatus(AssignmentStatus.ASSIGNED);
        return new DashboardMetrics(vehicles, inService, assigned, 0L, 0L, 0L);
    }

    public record DashboardMetrics(long vehicles,
            long vehiclesInService,
            long vehiclesAssigned,
            long assignments,
            long deadlines,
            long fuelMonth) {}
}
