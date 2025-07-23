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
        Document pdfDoc = new Document(PageSize.A4, 36, 36, 36, 36);
        try {
            PdfWriter.getInstance(pdfDoc, out);
            pdfDoc.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Paragraph title = new Paragraph("Nota Spese", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(10f);
            pdfDoc.add(title);

            // Intestazione principale
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            PdfPTable header = new PdfPTable(new float[]{1f, 2f});
            header.setWidthPercentage(100f);
            header.setSpacingAfter(10f);

            header.addCell(new Phrase("Dipendente:", labelFont));
            header.addCell(new Phrase(
                    report.getEmployee() != null
                            ? report.getEmployee().getFirstName() + " " + report.getEmployee().getLastName()
                            : "",
                    valueFont));

            header.addCell(new Phrase("Numero:", labelFont));
            header.addCell(new Phrase(report.getExpenseReportNum() == null ? "" : report.getExpenseReportNum(), valueFont));

            header.addCell(new Phrase("Data creazione:", labelFont));
            header.addCell(new Phrase(
                    report.getCreationDate() != null ? report.getCreationDate().toString() : "",
                    valueFont));

            header.addCell(new Phrase("Scopo:", labelFont));
            header.addCell(new Phrase(report.getPuorpose() != null ? report.getPuorpose() : "", valueFont));

            header.addCell(new Phrase("Periodo:", labelFont));
            header.addCell(new Phrase(
                    (report.getStartDate() != null ? report.getStartDate().toString() : "") +
                    " - " + (report.getEndDate() != null ? report.getEndDate().toString() : ""),
                    valueFont));

            pdfDoc.add(header);

            // Tabella righe di spesa
            PdfPTable table = new PdfPTable(new float[]{2f, 6f, 2f});
            table.setWidthPercentage(100f);
            table.addCell(new Phrase("Data", labelFont));
            table.addCell(new Phrase("Descrizione e Note", labelFont));
            table.addCell(new Phrase("Importo", labelFont));
            for (ExpenseItem it : items) {
            	table.addCell(new Phrase(it.getDate() != null ? it.getDate().toString() : "", valueFont));
                String desc = it.getDescription() == null ? "" : it.getDescription();
                if (it.getNote() != null && !it.getNote().isBlank()) {
                    desc += " - " + it.getNote();
                }
                table.addCell(new Phrase(desc, valueFont));
                table.addCell(new Phrase(it.getAmount() != null ? it.getAmount().toString() : "", valueFont));
            }
            table.setSpacingAfter(10f);
            pdfDoc.add(table);

            PdfPTable totals = new PdfPTable(new float[]{3f, 2f});
            totals.setWidthPercentage(60f);
            totals.setHorizontalAlignment(Element.ALIGN_RIGHT);

            totals.addCell(new Phrase("Totale Nota Spese", labelFont));
            totals.addCell(new Phrase(
                    report.getExpenseReportTotal() != null ? report.getExpenseReportTotal().toString() : "",
                    valueFont));

            totals.addCell(new Phrase("Totale Rimborsabile", labelFont));
            totals.addCell(new Phrase(
                    report.getReimbursableTotal() != null ? report.getReimbursableTotal().toString() : "",
                    valueFont));

            totals.addCell(new Phrase("Totale Non Rimborsabile", labelFont));
            totals.addCell(new Phrase(
                    report.getNonReimbursableTotal() != null ? report.getNonReimbursableTotal().toString() : "",
                    valueFont));

            totals.addCell(new Phrase("Metodo di Pagamento", labelFont));
            totals.addCell(new Phrase(
                    report.getPaymentMethodCode() != null ? report.getPaymentMethodCode().getDisplayName() : "",
                    valueFont));

            pdfDoc.add(totals);

            pdfDoc.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Errore nella creazione del PDF", e);
        }

        return out.toByteArray();
    }
    
}
