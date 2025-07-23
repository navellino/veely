package com.veely.service;

import com.veely.entity.ExpenseItem;
import com.veely.entity.ExpenseReport;
import com.veely.exception.ResourceNotFoundException;
import com.veely.model.ExpenseStatus;
import com.veely.repository.EmployeeRepository;
import com.veely.repository.ExpenseItemRepository;
import com.veely.repository.ExpenseReportRepository;
import com.veely.repository.ProjectRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
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
    private final DocumentService documentService;

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
        for (ExpenseItem it : current) {
            documentService.deleteExpenseItemDocuments(it.getId());
        }
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
        List<ExpenseItem> items = itemRepo.findByExpenseReportId(id);
        for (ExpenseItem it : items) {
            documentService.deleteExpenseItemDocuments(it.getId());
        }
        itemRepo.deleteAll(items);
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

    /**
     * Genera la versione PDF di una singola nota spese.
     */
    @Transactional(readOnly = true)
    public byte[] exportPdf(Long id) {
        ExpenseReport report = findByIdOrThrow(id);
        List<ExpenseItem> items = findItems(id);

        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        Document pdfDoc = new Document(PageSize.A4);
        try {
            PdfWriter.getInstance(pdfDoc, out);
            pdfDoc.open();

            pdfDoc.add(new Paragraph("Nota Spese"));
            pdfDoc.add(new Paragraph(" "));

            // Intestazione principale
            PdfPTable header = new PdfPTable(1);
            header.setWidthPercentage(100f);
            header.addCell("Dipendente: "
                    + (report.getEmployee() != null
                        ? report.getEmployee().getFirstName() + " " + report.getEmployee().getLastName()
                        : ""));
            header.addCell("Numero: " + report.getExpenseReportNum());
            header.addCell("Data creazione: " +
                    (report.getCreationDate() != null ? report.getCreationDate().toString() : ""));
            header.addCell("Scopo: " + (report.getPuorpose() != null ? report.getPuorpose() : ""));
            header.addCell("Periodo: " +
                    (report.getStartDate() != null ? report.getStartDate().toString() : "") +
                    " - " + (report.getEndDate() != null ? report.getEndDate().toString() : ""));
            pdfDoc.add(header);

            pdfDoc.add(new Paragraph(" "));

            // Tabella righe di spesa
            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100f);
            table.addCell("Data");
            table.addCell("Descrizione e Note");
            table.addCell("Importo");
            for (ExpenseItem it : items) {
                table.addCell(it.getDate() != null ? it.getDate().toString() : "");
                String desc = it.getDescription() == null ? "" : it.getDescription();
                if (it.getNote() != null && !it.getNote().isBlank()) {
                    desc += " - " + it.getNote();
                }
                table.addCell(desc);
                table.addCell(it.getAmount() != null ? it.getAmount().toString() : "");
            }
            pdfDoc.add(table);

            pdfDoc.add(new Paragraph(" "));

            pdfDoc.add(new Paragraph("Totale Nota Spese: " +
                    (report.getExpenseReportTotal() != null ? report.getExpenseReportTotal().toString() : "")));
            pdfDoc.add(new Paragraph("Totale Rimborsabile: " +
                    (report.getReimbursableTotal() != null ? report.getReimbursableTotal().toString() : "")));
            pdfDoc.add(new Paragraph("Totale Non Rimborsabile: " +
                    (report.getNonReimbursableTotal() != null ? report.getNonReimbursableTotal().toString() : "")));
            pdfDoc.add(new Paragraph("Metodo di Pagamento: " +
                    (report.getPaymentMethodCode() != null ? report.getPaymentMethodCode().getDisplayName() : "")));

            pdfDoc.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Errore nella creazione del PDF", e);
        }

        return out.toByteArray();
    }
    
}
