package com.veely.controller;

import com.veely.entity.Employee;
import com.veely.entity.EmployeeRole;
import com.veely.entity.ExpenseItem;
import com.veely.entity.ExpenseReport;
import com.veely.entity.Supplier;
import com.veely.model.ExpenseStatus;
import com.veely.service.EmployeeService;
import com.veely.service.ExpenseReportService;
import com.veely.service.SupplierService;

import com.veely.model.PaymentMethod;
import com.veely.entity.Project;
import com.veely.service.ProjectService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/fleet/expense-reports")
@RequiredArgsConstructor
public class ExpenseReportController {

    private final ExpenseReportService reportService;
    private final EmployeeService employeeService;
    private final SupplierService supplierService;
    private final ProjectService projectService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("reports", reportService.findAll());
        return "fleet/expense_reports/index";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("report", new ExpenseReport());
        model.addAttribute("items", new ArrayList<ExpenseItem>());
        model.addAttribute("employees", employeeService.findAll());
        model.addAttribute("statuses", ExpenseStatus.values());
        model.addAttribute("paymentMethods", PaymentMethod.values());
        model.addAttribute("suppliers", supplierService.findAll());
        model.addAttribute("projects", projectService.findAll());
        return "fleet/expense_reports/form";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("report") ExpenseReport report, BindingResult binding,
                         @RequestParam("itemDesc") List<String> itemDesc,
                         @RequestParam("itemAmount") List<String> itemAmount,
                         @RequestParam("itemDate") List<String> itemDate,
                         @RequestParam("itemInvoice") List<String> itemInvoice,
                         @RequestParam("itemSupplierId") List<String> itemSupplier,
                         @RequestParam("itemProjectId") List<String> itemProject,
                         @RequestParam("itemNote") List<String> itemNote,
                         Model model) {
        if (binding.hasErrors()) {
            model.addAttribute("employees", employeeService.findAll());
            model.addAttribute("statuses", ExpenseStatus.values());
            return "fleet/expense_reports/form";
        }
        List<ExpenseItem> items = buildItems(itemDesc, itemAmount, itemDate, itemInvoice, itemSupplier, itemProject, itemNote);
        ExpenseReport saved = reportService.create(report, items);
        return "redirect:/fleet/expense-reports/" + saved.getId() + "/edit";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        ExpenseReport r = reportService.findByIdOrThrow(id);
        model.addAttribute("report", r);
        model.addAttribute("items", reportService.findItems(id));
        model.addAttribute("employees", employeeService.findAll());
        model.addAttribute("statuses", ExpenseStatus.values());
        model.addAttribute("paymentMethods", PaymentMethod.values());
        model.addAttribute("suppliers", supplierService.findAll());
        model.addAttribute("projects", projectService.findAll());
        return "fleet/expense_reports/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("report") ExpenseReport report,
                         BindingResult binding,
                         @RequestParam("itemDesc") List<String> itemDesc,
                         @RequestParam("itemAmount") List<String> itemAmount,
                         @RequestParam("itemDate") List<String> itemDate,
                         @RequestParam("itemInvoice") List<String> itemInvoice,
                         @RequestParam("itemSupplierId") List<String> itemSupplier,
                         @RequestParam("itemProjectId") List<String> itemProject,
                         @RequestParam("itemNote") List<String> itemNote,
                         Model model) {
        if (binding.hasErrors()) {
            model.addAttribute("employees", employeeService.findAll());
            model.addAttribute("statuses", ExpenseStatus.values());
            model.addAttribute("paymentMethods", PaymentMethod.values());
            model.addAttribute("suppliers", supplierService.findAll());
            model.addAttribute("projects", projectService.findAll());
            return "fleet/expense_reports/form";
        }
        List<ExpenseItem> items = buildItems(itemDesc, itemAmount, itemDate, itemInvoice, itemSupplier, itemProject, itemNote);
        reportService.update(id, report, items);
        return "redirect:/fleet/expense-reports/" + id + "/edit";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        reportService.delete(id);
        return "redirect:/fleet/expense-reports";
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id, @RequestParam Long employeeId) {
        ExpenseReport r = reportService.findByIdOrThrow(id);
        Employee approver = employeeService.findByIdOrThrow(employeeId);
        EmployeeRole role = approver.getEmployeeRole();
        if (role != null && "administrator".equalsIgnoreCase(role.getName())) {
            r.setExpenseStatus(ExpenseStatus.Approved);
        }
        return "redirect:/fleet/expense-reports/" + id + "/edit";
    }

    private List<ExpenseItem> buildItems(List<String> descs, List<String> amounts, List<String> dates,
            List<String> invoices, List<String> suppliers, List<String> projects, List<String> notes) {
        List<ExpenseItem> list = new ArrayList<>();
        int size = Math.min(Math.min(descs.size(), amounts.size()), Math.min(dates.size(), Math.min(invoices.size(), Math.min(suppliers.size(), Math.min(projects.size(), notes.size())))));
        for (int i = 0; i < size; i++) {
            String desc = descs.get(i);
            String amountStr = amounts.get(i);
            String dateStr = dates.get(i);
            String invoice = invoices.get(i);
            String supplierId = suppliers.get(i);
            String projectId = projects.get(i);
            String note = notes.get(i);

            if ((desc == null || desc.isBlank()) &&
                (amountStr == null || amountStr.isBlank()) &&
                (dateStr == null || dateStr.isBlank()) &&
                (invoice == null || invoice.isBlank()) &&
                (supplierId == null || supplierId.isBlank()) &&
                (projectId == null || projectId.isBlank()) &&
                (note == null || note.isBlank())) {
                continue;
            }
            ExpenseItem it = new ExpenseItem();
            it.setDescription(desc);
            if (amountStr != null && !amountStr.isBlank()) {
                it.setAmount(new BigDecimal(amountStr.replace(',', '.')));
            }
            if (dateStr != null && !dateStr.isBlank()) {
                it.setDate(LocalDate.parse(dateStr));
            }
            if (invoice != null && !invoice.isBlank()) {
                it.setInvoiceNumber(invoice);
            }
            if (supplierId != null && !supplierId.isBlank()) {
                Supplier s = supplierService.findByIdOrThrow(Long.parseLong(supplierId));
                it.setSupplier(s);
            }
            if (projectId != null && !projectId.isBlank()) {
                Project p = projectService.findByIdOrThrow(Long.parseLong(projectId));
                it.setProject(p);
            }
            if (note != null && !note.isBlank()) {
                it.setNote(note);
            }
            list.add(it);
        }
        return list;
    }
}
