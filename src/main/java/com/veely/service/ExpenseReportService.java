package com.veely.service;

import com.veely.entity.ExpenseItem;
import com.veely.entity.ExpenseReport;
import com.veely.exception.ResourceNotFoundException;
import com.veely.model.ExpenseStatus;
import com.veely.repository.EmployeeRepository;
import com.veely.repository.ExpenseItemRepository;
import com.veely.repository.ExpenseReportRepository;
import com.veely.repository.ProjectRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpenseReportService {

    private final ExpenseReportRepository reportRepo;
    private final ExpenseItemRepository itemRepo;
    private final EmployeeRepository employeeRepo;
    private final ProjectRepository projectRepo;

    public ExpenseReport create(ExpenseReport report, List<ExpenseItem> items) {
    	 if (report.getEmployee() != null && report.getEmployee().getId() != null) {
             employeeRepo.findById(report.getEmployee().getId()).ifPresent(report::setEmployee);
         }
         if (report.getProject() != null && report.getProject().getId() != null) {
             projectRepo.findById(report.getProject().getId()).ifPresent(report::setProject);
         } else {
             report.setProject(null);
         }
        report.setExpenseStatus(ExpenseStatus.Draft);
        report.setExpenseReportTotal(sumItems(items));
        report.setNonReimbursableTotal(report.getExpenseReportTotal() - report.getReimbursableTotal());
        ExpenseReport saved = reportRepo.save(report);
        for (ExpenseItem item : items) {
            item.setExpenseReport(saved);
            itemRepo.save(item);
        }
        return saved;
    }

    public ExpenseReport update(Long id, ExpenseReport payload, List<ExpenseItem> items) {
        ExpenseReport existing = findByIdOrThrow(id);
        existing.setPuorpose(payload.getPuorpose());
        existing.setStartDate(payload.getStartDate());
        existing.setEndDate(payload.getEndDate());
        existing.setPaymentMethodCode(payload.getPaymentMethodCode());
        existing.setExpenseStatus(payload.getExpenseStatus());
        if (payload.getEmployee() != null && payload.getEmployee().getId() != null) {
            employeeRepo.findById(payload.getEmployee().getId()).ifPresent(existing::setEmployee);
        } else {
            existing.setEmployee(null);
        }
        if (payload.getProject() != null && payload.getProject().getId() != null) {
            projectRepo.findById(payload.getProject().getId()).ifPresent(existing::setProject);
        } else {
            existing.setProject(null);
        }
        existing.setReimbursableTotal(payload.getReimbursableTotal());
        existing.setExpenseReportTotal(sumItems(items));
        existing.setNonReimbursableTotal(existing.getExpenseReportTotal() - existing.getReimbursableTotal());
        // replace items
        List<ExpenseItem> current = itemRepo.findByExpenseReportId(id);
        itemRepo.deleteAll(current);
        for (ExpenseItem item : items) {
            item.setExpenseReport(existing);
            itemRepo.save(item);
        }
        return existing;
    }

    @Transactional(readOnly = true)
    public ExpenseReport findByIdOrThrow(Long id) {
        return reportRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nota spese non trovata: " + id));
    }

    @Transactional(readOnly = true)
    public List<ExpenseReport> findAll() {
        return reportRepo.findAll();
    }

    @Transactional(readOnly = true)
    public List<ExpenseItem> findItems(Long reportId) {
        return itemRepo.findByExpenseReportId(reportId);
    }

    public void delete(Long id) {
        ExpenseReport r = findByIdOrThrow(id);
        itemRepo.deleteAll(itemRepo.findByExpenseReportId(id));
        reportRepo.delete(r);
    }
    
    private long sumItems(List<ExpenseItem> items) {
        return items.stream()
                .filter(i -> i.getAmount() != null)
                .map(i -> i.getAmount().longValue())
                .reduce(0L, Long::sum);
    }
    
    @Transactional(readOnly = true)
    public String getNextExpenseReportBase() {
        Long maxId = reportRepo.findMaxId();
        long next = maxId == null ? 1 : maxId + 1;
        int year = LocalDate.now().getYear();
        return String.format("%03d/%d/", next, year);
    }

}
