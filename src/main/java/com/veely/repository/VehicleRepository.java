package com.veely.repository;

import com.veely.entity.Vehicle;
import com.veely.model.VehicleStatus;
import com.veely.model.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    List<Vehicle> findByType(VehicleType type);

    /** Veicoli con stato specifico (es. AVAILABLE). */
    List<Vehicle> findByStatus(VehicleStatus status);
    
    long countByStatus(VehicleStatus status);
}
