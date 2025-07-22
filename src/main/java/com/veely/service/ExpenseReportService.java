package com.veely.service;

import com.veely.entity.ExpenseItem;
import com.veely.entity.ExpenseReport;
import com.veely.exception.ResourceNotFoundException;
import com.veely.model.ExpenseStatus;
import com.veely.repository.ExpenseItemRepository;
import com.veely.repository.ExpenseReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpenseReportService {

    private final ExpenseReportRepository reportRepo;
    private final ExpenseItemRepository itemRepo;

    public ExpenseReport create(ExpenseReport report, List<ExpenseItem> items) {
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
        existing.setEmployee(payload.getEmployee());
        existing.setProject(payload.getProject());
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
}
