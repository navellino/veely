package com.veely.repository;

import com.veely.entity.ExpenseReport;
import com.veely.model.ExpenseStatus;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExpenseReportRepository extends JpaRepository<ExpenseReport, Long> {
	
	@Query("select coalesce(max(er.id),0) from ExpenseReport er")
    Long findMaxId();
	
	List<ExpenseReport> findTop5ByExpenseStatusOrderByReportSubmitDateDesc(ExpenseStatus status);

    @Query("select year(er.creationDate) as yr, month(er.creationDate) as mth, sum(er.expenseReportTotal) " +
           "from ExpenseReport er where er.creationDate between :start and :end " +
           "group by year(er.creationDate), month(er.creationDate) " +
           "order by yr, mth")
    List<Object[]> sumTotalsByMonth(@Param("start") LocalDate start,
                                    @Param("end") LocalDate end);
}
