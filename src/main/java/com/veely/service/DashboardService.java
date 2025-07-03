package com.veely.service;

import com.veely.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final VehicleRepository vehicleRepo;

    public DashboardMetrics getMetrics() {
        long vehicles = vehicleRepo.count();
        return new DashboardMetrics(vehicles, 0L, 0L, 0L);
    }

    public record DashboardMetrics(long vehicles, long assignments, long deadlines, long fuelMonth) {}
}
