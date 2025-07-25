package com.veely.repository;

import com.veely.entity.Refuel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RefuelRepository extends JpaRepository<Refuel, Long> {

    @Query("select year(r.date) as yr, month(r.date) as mth, sum(r.amount) " +
            "from Refuel r where r.date between :start and :end " +
            "group by year(r.date), month(r.date) " +
            "order by yr, mth")
    List<Object[]> sumAmountByMonth(@Param("start") LocalDate start,
                                    @Param("end") LocalDate end);
    
    /** Total fuel cost between the given dates (inclusive). */
    @Query("select coalesce(sum(r.amount),0) from Refuel r where r.date between :start and :end")
    java.math.BigDecimal sumAmountBetween(@Param("start") LocalDate start,
                                          @Param("end") LocalDate end);
}
