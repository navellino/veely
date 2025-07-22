package com.veely.service;

import com.veely.model.AssignmentStatus;
import com.veely.model.AssignmentType;
import com.veely.model.ExpenseStatus;
import com.veely.model.VehicleStatus;
import com.veely.repository.AssignmentRepository;
import com.veely.repository.ExpenseItemRepository;
import com.veely.repository.ExpenseReportRepository;
import com.veely.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class DashboardService {

    private final VehicleRepository vehicleRepo;
    private final AssignmentRepository assignmentRepo;
    private final DeadlineService deadlineService;
    private final CorrespondenceService correspondenceService;
    private final ExpenseItemRepository expenseItemRepo;
    private final ExpenseReportRepository expenseReportRepo;

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
        String lastIncoming = correspondenceService.getLastIncomingProtocol();
        String lastOutgoing = correspondenceService.getLastOutgoingProtocol();
        return new DashboardMetrics(vehicles, inService, assigned,
        		totalAssignments, longActive, shortActive,
                deadlines60, deadlines30, deadlines,
                lastIncoming, lastOutgoing);
    }
    
    /** Returns vehicle counts grouped by status. */
    public java.util.Map<String, Long> getVehicleStatusCounts() {
        java.util.Map<String, Long> map = new java.util.LinkedHashMap<>();
        for (VehicleStatus vs : VehicleStatus.values()) {
            map.put(vs.getDisplayName(), vehicleRepo.countByStatus(vs));
        }
        return map;
    }

    /** Last {@code months} monthly fuel costs aggregated from expense items. */
    public java.util.List<MonthAmount> getFuelCosts(int months) {
        java.time.YearMonth start = java.time.YearMonth.now().minusMonths(months - 1);
        java.time.LocalDate from = start.atDay(1);
        java.time.LocalDate to = java.time.LocalDate.now();
        java.util.Map<java.time.YearMonth, Long> tmp = new java.util.LinkedHashMap<>();
        for (Object[] row : expenseItemRepo.sumByMonth(from, to)) {
            int y = ((Number) row[0]).intValue();
            int m = ((Number) row[1]).intValue();
            long sum = ((Number) row[2]).longValue();
            tmp.put(java.time.YearMonth.of(y, m), sum);
        }
        java.util.List<MonthAmount> result = new java.util.ArrayList<>();
        for (int i = 0; i < months; i++) {
            java.time.YearMonth ym = start.plusMonths(i);
            long val = tmp.getOrDefault(ym, 0L);
            result.add(new MonthAmount(ym.toString(), val));
        }
        return result;
    }

    /** Last {@code months} total expense report amounts. */
    public java.util.List<MonthAmount> getExpenseReportTotals(int months) {
        java.time.YearMonth start = java.time.YearMonth.now().minusMonths(months - 1);
        java.time.LocalDate from = start.atDay(1);
        java.time.LocalDate to = java.time.LocalDate.now();
        java.util.Map<java.time.YearMonth, Long> tmp = new java.util.LinkedHashMap<>();
        for (Object[] row : expenseReportRepo.sumTotalsByMonth(from, to)) {
            int y = ((Number) row[0]).intValue();
            int m = ((Number) row[1]).intValue();
            long sum = ((Number) row[2]).longValue();
            tmp.put(java.time.YearMonth.of(y, m), sum);
        }
        java.util.List<MonthAmount> result = new java.util.ArrayList<>();
        for (int i = 0; i < months; i++) {
            java.time.YearMonth ym = start.plusMonths(i);
            long val = tmp.getOrDefault(ym, 0L);
            result.add(new MonthAmount(ym.toString(), val));
        }
        return result;
    }

    /** Upcoming deadlines sorted by due date. */
    public java.util.List<com.veely.model.DeadlineItem> getUpcomingDeadlines(int limit) {
        return deadlineService.getAllDeadlines().stream()
                .filter(d -> d.dueDate() != null)
                .sorted(java.util.Comparator.comparing(com.veely.model.DeadlineItem::dueDate))
                .limit(limit)
                .toList();
    }

    /** Latest expense reports waiting for approval. */
    public java.util.List<com.veely.entity.ExpenseReport> getPendingExpenseReports(int limit) {
        return expenseReportRepo
                .findTop5ByExpenseStatusOrderByReportSubmitDateDesc(ExpenseStatus.Submitted)
                .stream()
                .limit(limit)
                .toList();
    }

    public record MonthAmount(String month, long total) {}

    public record DashboardMetrics(long vehicles,
            long vehiclesInService,
            long vehiclesAssigned,
            long assignments,
            long longAssignmentsActive,
            long shortAssignmentsActive,
            long deadlines60,
            long deadlines30,
            long deadlines,
            String lastIncomingProtocol,
            String lastOutgoingProtocol) {}
}
