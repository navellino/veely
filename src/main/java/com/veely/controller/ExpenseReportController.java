package com.veely.controller;

import com.veely.entity.Employee;
import com.veely.entity.EmployeeRole;
import com.veely.entity.ExpenseItem;
import com.veely.entity.ExpenseReport;
import com.veely.model.ExpenseStatus;
import com.veely.service.EmployeeService;
import com.veely.service.ExpenseReportService;
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
        return "fleet/expense_reports/form";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("report") ExpenseReport report, BindingResult binding,
                         @RequestParam("itemDesc") List<String> itemDesc,
                         @RequestParam("itemAmount") List<String> itemAmount,
                         @RequestParam("itemDate") List<String> itemDate,
                         Model model) {
        if (binding.hasErrors()) {
            model.addAttribute("employees", employeeService.findAll());
            model.addAttribute("statuses", ExpenseStatus.values());
            return "fleet/expense_reports/form";
        }
        List<ExpenseItem> items = buildItems(itemDesc, itemAmount, itemDate);
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
        return "fleet/expense_reports/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("report") ExpenseReport report,
                         BindingResult binding,
                         @RequestParam("itemDesc") List<String> itemDesc,
                         @RequestParam("itemAmount") List<String> itemAmount,
                         @RequestParam("itemDate") List<String> itemDate,
                         Model model) {
        if (binding.hasErrors()) {
            model.addAttribute("employees", employeeService.findAll());
            model.addAttribute("statuses", ExpenseStatus.values());
            return "fleet/expense_reports/form";
        }
        List<ExpenseItem> items = buildItems(itemDesc, itemAmount, itemDate);
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

    private List<ExpenseItem> buildItems(List<String> descs, List<String> amounts, List<String> dates) {
        List<ExpenseItem> list = new ArrayList<>();
        int size = Math.min(descs.size(), Math.min(amounts.size(), dates.size()));
        for (int i = 0; i < size; i++) {
            String desc = descs.get(i);
            String amountStr = amounts.get(i);
            String dateStr = dates.get(i);

            if ((desc == null || desc.isBlank()) &&
                (amountStr == null || amountStr.isBlank()) &&
                (dateStr == null || dateStr.isBlank())) {
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
            list.add(it);
        }
        return list;
    }
}
