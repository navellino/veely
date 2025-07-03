package com.veely.repository;

import com.veely.entity.Assignment;
import com.veely.model.AssignmentStatus;
import com.veely.model.AssignmentType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    /** Assegnazioni attive per un rapporto di lavoro (dipendente). */
    List<Assignment> findByEmploymentIdAndStatus(Long employmentId, AssignmentStatus status);

    /** Assegnazioni future/prenotate su un veicolo. */
    List<Assignment> findByVehicleIdAndStartDateAfter(Long vehicleId, java.time.LocalDate date);

    Page<Assignment> findByStatus(AssignmentStatus status, Pageable pageable);

    /** Lista di assegnazioni filtrate per tipologia. */
    List<Assignment> findByTypeOrderByStartDateDesc(AssignmentType type);

    /** Numero di veicoli distinti con assegnazioni in uno stato specifico. */
    @Query("SELECT COUNT(DISTINCT a.vehicle.id) FROM Assignment a WHERE a.status = :status")
    long countDistinctVehicleByStatus(AssignmentStatus status);
}
