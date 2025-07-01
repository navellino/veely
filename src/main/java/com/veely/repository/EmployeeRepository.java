package com.veely.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.veely.entity.Employee;
import com.veely.model.EmploymentStatus;
import com.veely.model.UserRole;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

	Optional<Employee> findByEmail(String email);
	
	/** Controlla se esiste già un altro utente con lo stesso fiscalCode (CF). */
    boolean existsByFiscalCode(String fiscalCode);
	
    Page<Employee> findByRole(UserRole role, Pageable pageable);
    Page<Employee> findByFirstNameIgnoreCaseContainingOrLastNameIgnoreCaseContaining(
        String firstName, String lastName, Pageable pageable);
    
    @Query("select e from Employee e left join e.employments emp with emp.status = :status where emp.id is null")
    List<Employee> findAvailableForEmployment(@Param("status") EmploymentStatus status);
}
