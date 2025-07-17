package com.veely.repository;

import com.veely.entity.ExpenseItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseItemRepository extends JpaRepository<ExpenseItem, Long> {
    List<ExpenseItem> findByExpenseReportId(Long reportId);
}
