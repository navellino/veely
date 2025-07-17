package com.veely.repository;

import com.veely.entity.ExpenseReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseReportRepository extends JpaRepository<ExpenseReport, Long> {
}
