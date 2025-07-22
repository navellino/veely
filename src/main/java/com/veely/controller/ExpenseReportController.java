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
    	List<Employee> employees = employeeService.findAll();
        ExpenseReport report = new ExpenseReport();
        String baseNum = reportService.getNextExpenseReportBase();
        if (!employees.isEmpty()) {
            report.setExpenseReportNum(baseNum + getInitials(employees.get(0)));
        } else {
            report.setExpenseReportNum(baseNum);
        }
        model.addAttribute("report", report);
        model.addAttribute("baseReportNum", baseNum);
        model.addAttribute("items", new ArrayList<ExpenseItem>());
        model.addAttribute("employees", employees);
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
                         @RequestParam("itemNote") List<String> itemNote,
                         Model model) {
        if (binding.hasErrors()) {
            model.addAttribute("employees", employeeService.findAll());
            model.addAttribute("statuses", ExpenseStatus.values());
            return "fleet/expense_reports/form";
        }
        List<ExpenseItem> items = buildItems(itemDesc, itemAmount, itemDate, itemInvoice, itemSupplier, itemNote);
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
        List<ExpenseItem> items = buildItems(itemDesc, itemAmount, itemDate, itemInvoice, itemSupplier, itemNote);
        reportService.update(id, report, items);
        return "redirect:/fleet/expense-reports/" + id + "/edit";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        reportService.delete(id);
        return "redirect:/fleet/expense-reports";
    }

    @PostMapping("/{id}/approve")
    public String toggleApprove(@PathVariable Long id) {
        ExpenseReport r = reportService.findByIdOrThrow(id);
        if (r.getExpenseStatus() == ExpenseStatus.Approved) {
            r.setExpenseStatus(ExpenseStatus.Draft);
        } else {
            r.setExpenseStatus(ExpenseStatus.Approved);
        }
        return "redirect:/fleet/expense-reports/" + id + "/edit";
    }

    private List<ExpenseItem> buildItems(List<String> descs, List<String> amounts, List<String> dates,
    		List<String> invoices, List<String> suppliers, List<String> notes) {
        List<ExpenseItem> list = new ArrayList<>();
        int size = Math.min(Math.min(descs.size(), amounts.size()), Math.min(dates.size(), Math.min(invoices.size(), Math.min(suppliers.size(), notes.size()))));
        for (int i = 0; i < size; i++) {
            String desc = descs.get(i);
            String amountStr = amounts.get(i);
            String dateStr = dates.get(i);
            String invoice = invoices.get(i);
            String supplierId = suppliers.get(i);
            String note = notes.get(i);

            if ((desc == null || desc.isBlank()) &&
                (amountStr == null || amountStr.isBlank()) &&
                (dateStr == null || dateStr.isBlank()) &&
                (invoice == null || invoice.isBlank()) &&
                (supplierId == null || supplierId.isBlank()) &&
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
            if (note != null && !note.isBlank()) {
                it.setNote(note);
            }
            list.add(it);
        }
        return list;
    }
    
    private String getInitials(Employee e) {
        if (e == null) {
            return "";
        }
        String first = e.getFirstName() == null ? "" : e.getFirstName();
        String last = e.getLastName() == null ? "" : e.getLastName();
        char li = last.isEmpty() ? ' ' : Character.toUpperCase(last.charAt(0));
        char fi = first.isEmpty() ? ' ' : Character.toUpperCase(first.charAt(0));
        return "" + li + fi;
    }
}
