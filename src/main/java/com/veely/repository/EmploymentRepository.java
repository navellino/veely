package com.veely.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.veely.entity.Employee;
import com.veely.entity.Employment;
import com.veely.model.EmploymentStatus;

public interface EmploymentRepository extends JpaRepository<Employment, Long> {
	/** Rapporto di lavoro attivo per una persona – ce ne può essere al massimo uno. */
    Optional<Employment> findFirstByEmployeeIdAndStatus(Long employeeId, EmploymentStatus status);

    /** Tutti i rapporti attivi in una certa filiale. */
    List<Employment> findByBranchAndStatus(String branch, EmploymentStatus status);
    
    /**
     * Filtra i rapporti per stato (ACTIVE, TERMINATED, SUSPENDED) con paginazione
     */
    Page<Employment> findByStatus(EmploymentStatus status, Pageable pageable);

    /**
     * Cerca per jobTitle contenente la keyword (ignore case) con paginazione
     */
    Page<Employment> findByJobTitleIgnoreCaseContaining(String jobTitle, Pageable pageable);
    
    
    @Query("""
            select emp from Employment emp
            join emp.employee person
            left join emp.workplace w
            where lower(person.firstName) like lower(concat('%', :kw, '%'))
               or lower(person.lastName) like lower(concat('%', :kw, '%'))
               or lower(emp.matricola) like lower(concat('%', :kw, '%'))
               or lower(emp.jobTitle) like lower(concat('%', :kw, '%'))
               or lower(emp.contractLevel) like lower(concat('%', :kw, '%'))
               or lower(emp.branch) like lower(concat('%', :kw, '%'))
               or lower(coalesce(w.siteName,'')) like lower(concat('%', :kw, '%'))
        """)
        Page<Employment> searchByKeyword(@Param("kw") String keyword, Pageable pageable);

        @Query("""
            select emp from Employment emp
            join emp.employee person
            left join emp.workplace w
            where emp.status = :status and (
                lower(person.firstName) like lower(concat('%', :kw, '%'))
                or lower(person.lastName) like lower(concat('%', :kw, '%'))
                or lower(emp.matricola) like lower(concat('%', :kw, '%'))
                or lower(emp.jobTitle) like lower(concat('%', :kw, '%'))
                or lower(emp.contractLevel) like lower(concat('%', :kw, '%'))
                or lower(emp.branch) like lower(concat('%', :kw, '%'))
                or lower(coalesce(w.siteName,'')) like lower(concat('%', :kw, '%'))
            )
        """)
        Page<Employment> searchByStatusAndKeyword(@Param("status") EmploymentStatus status,
                                                  @Param("kw") String keyword,
                                                  Pageable pageable);
    
    
    List<Employment> findByEmployeeId(Long employeeId);
    
    // Se Employment ha un campo Employee employee, usiamo la property path employee.id
    List<Employment> findByEmployeeIdIn(Collection<Long> employeeIds);
    
    @Query("select e from Employee e left join e.employments emp with emp.status = :status where emp.id is null")
    List<Employee> findAvailableForEmployment(@Param("status") EmploymentStatus status);
    
}
