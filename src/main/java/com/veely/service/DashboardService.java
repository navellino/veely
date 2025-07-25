package com.veely.service;

import com.veely.model.AssignmentStatus;
import com.veely.model.AssignmentType;
import com.veely.model.DeadlineItem;
import com.veely.model.ExpenseStatus;
import com.veely.model.VehicleStatus;
import com.veely.repository.AssignmentRepository;
import com.veely.repository.ExpenseItemRepository;
import com.veely.repository.ExpenseReportRepository;
import com.veely.repository.RefuelRepository;
import com.veely.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    private final RefuelRepository refuelRepo;

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
        
        YearMonth ym = java.time.YearMonth.now();
       	LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        BigDecimal fuelMonth = refuelRepo.sumAmountBetween(start, end);
        
        return new DashboardMetrics(vehicles, inService, assigned,
        		totalAssignments, longActive, shortActive,
                deadlines60, deadlines30, deadlines,
                lastIncoming, lastOutgoing, fuelMonth);
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
    public List<MonthAmount> getFuelCosts(int months) {
        java.time.YearMonth start = java.time.YearMonth.now().minusMonths(months - 1);
        java.time.LocalDate from = start.atDay(1);
        LocalDate to = LocalDate.now();
        Map<java.time.YearMonth, java.math.BigDecimal> tmp = new java.util.LinkedHashMap<>();
        for (Object[] row : refuelRepo.sumAmountByMonth(from, to)) {
            int y = ((Number) row[0]).intValue();
            int m = ((Number) row[1]).intValue();
            BigDecimal sum = new java.math.BigDecimal(row[2].toString());
            tmp.put(java.time.YearMonth.of(y, m), sum);
        }
        List<MonthAmount> result = new java.util.ArrayList<>();
        for (int i = 0; i < months; i++) {
        	YearMonth ym = start.plusMonths(i);
        	BigDecimal val = tmp.getOrDefault(ym, BigDecimal.ZERO);
        	result.add(new MonthAmount(ym.toString(), val));
        }
        return result;
    }

    /** Last {@code months} total expense report amounts. */
    public List<MonthAmount> getExpenseReportTotals(int months) {
        YearMonth start = YearMonth.now().minusMonths(months - 1);
        LocalDate from = start.atDay(1);
        java.time.LocalDate to = java.time.LocalDate.now();
        Map<YearMonth, java.math.BigDecimal> tmp = new LinkedHashMap<>();
        for (Object[] row : expenseReportRepo.sumTotalsByMonth(from, to)) {
            int y = ((Number) row[0]).intValue();
            int m = ((Number) row[1]).intValue();
            BigDecimal sum = new java.math.BigDecimal(row[2].toString());
            tmp.put(YearMonth.of(y, m), sum);
        }
        List<MonthAmount> result = new java.util.ArrayList<>();
        for (int i = 0; i < months; i++) {
            YearMonth ym = start.plusMonths(i);
            BigDecimal val = tmp.getOrDefault(ym, java.math.BigDecimal.ZERO);
            result.add(new MonthAmount(ym.toString(), val));
        }
        return result;
    }

    /** Upcoming deadlines sorted by due date. */
    public List<DeadlineItem> getUpcomingDeadlines(int limit) {
        return deadlineService.getAllDeadlines().stream()
                .filter(d -> d.dueDate() != null)
                .sorted(java.util.Comparator.comparing(DeadlineItem::dueDate))
                .limit(limit)
                .toList();
    }

    /** Latest expense reports waiting for approval. */
    public List<com.veely.entity.ExpenseReport> getPendingExpenseReports(int limit) {
        return expenseReportRepo
                .findTop5ByExpenseStatusOrderByReportSubmitDateDesc(ExpenseStatus.Submitted)
                .stream()
                .limit(limit)
                .toList();
    }

    public record MonthAmount(String month, java.math.BigDecimal total) {}

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
            String lastOutgoingProtocol,
            java.math.BigDecimal fuelMonth) {}
}
