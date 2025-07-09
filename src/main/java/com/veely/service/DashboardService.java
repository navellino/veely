package com.veely.service;

import com.veely.model.AssignmentStatus;
import com.veely.model.AssignmentType;
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
    private final DeadlineService deadlineService;

    public DashboardMetrics getMetrics() {
        long vehicles = vehicleRepo.count();
        long inService = vehicleRepo.countByStatus(VehicleStatus.IN_SERVICE);
        long assigned = assignmentRepo.countDistinctVehicleByStatus(AssignmentStatus.ASSIGNED);
        long longActive = assignmentRepo.countByTypeAndStatus(AssignmentType.LONG_TERM, AssignmentStatus.ASSIGNED);
        long shortActive = assignmentRepo.countByTypeAndStatus(AssignmentType.SHORT_TERM, AssignmentStatus.ASSIGNED);
        long totalAssignments = assignmentRepo.count();
        long deadlines30 = deadlineService.countDeadlinesWithinDays(30);
        long deadlines60 = deadlineService.countDeadlinesWithinDays(60) - deadlines30;
        if (deadlines60 < 0) {
            deadlines60 = 0;
        }
        long deadlines = deadlines60 + deadlines30;
        return new DashboardMetrics(vehicles, inService, assigned,
        		totalAssignments, longActive, shortActive,
                deadlines60, deadlines30, deadlines);
    }

    public record DashboardMetrics(long vehicles,
            long vehiclesInService,
            long vehiclesAssigned,
            long assignments,
            long longAssignmentsActive,
            long shortAssignmentsActive,
            long deadlines60,
            long deadlines30,
            long deadlines) {}
}
