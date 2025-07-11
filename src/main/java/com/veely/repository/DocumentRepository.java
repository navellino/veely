package com.veely.repository;


import com.veely.entity.Document;
import com.veely.model.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByVehicleId(Long vehicleId);

    List<Document> findByEmploymentIdAndType(Long employmentId, DocumentType type);

    List<Document> findByEmployeeIdAndType(Long employeeId, DocumentType type);
    
    List<Document> findByAssignmentId(Long assignmentId);
    
 // Restituisce tutti i documenti anagrafici legati a un employee
    List<Document> findByEmployeeId(Long employeeId);

    // (utile anche per Employment, Vehicle, Assignment)
    List<Document> findByEmploymentId(Long employmentId);
    
    Optional<Document> findByCorrespondenceId(Long correspondenceId);

}
