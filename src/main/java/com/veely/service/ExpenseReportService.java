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
        if (report.getReimbursableTotal() == null) {
            report.setReimbursableTotal(java.math.BigDecimal.ZERO);
        }
        report.setNonReimbursableTotal(report.getExpenseReportTotal().subtract(report.getReimbursableTotal()));
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
        existing.setExpenseReportNum(payload.getExpenseReportNum());
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
        if (existing.getReimbursableTotal() == null) {
            existing.setReimbursableTotal(java.math.BigDecimal.ZERO);
        }
        existing.setNonReimbursableTotal(existing.getExpenseReportTotal().subtract(existing.getReimbursableTotal()));
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
    
    @Transactional(readOnly = true)
    public ExpenseItem findItemById(Long id) {
        return itemRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voce spesa non trovata: " + id));
    }

    public void delete(Long id) {
        ExpenseReport r = findByIdOrThrow(id);
        itemRepo.deleteAll(itemRepo.findByExpenseReportId(id));
        reportRepo.delete(r);
    }
    
    private java.math.BigDecimal sumItems(List<ExpenseItem> items) {
        java.math.BigDecimal total = java.math.BigDecimal.ZERO;
        for (ExpenseItem i : items) {
            if (i.getAmount() != null) {
                total = total.add(i.getAmount());
            }
        }
        return total;
    }
    
    @Transactional(readOnly = true)
    public String getNextExpenseReportBase() {
        Long maxId = reportRepo.findMaxId();
        long next = maxId == null ? 1 : maxId + 1;
        int year = LocalDate.now().getYear();
        return String.format("%03d/%d/", next, year);
    }

}
