package com.veely.repository;

import com.veely.entity.ExpenseReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ExpenseReportRepository extends JpaRepository<ExpenseReport, Long> {
	
	@Query("select coalesce(max(er.id),0) from ExpenseReport er")
    Long findMaxId();
}
