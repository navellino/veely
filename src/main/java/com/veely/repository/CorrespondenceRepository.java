package com.veely.repository;

import com.veely.entity.Correspondence;
import com.veely.model.CorrespondenceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CorrespondenceRepository extends JpaRepository<Correspondence, Long> {
    @Query("select max(c.progressivo) from Correspondence c where c.anno = :anno and c.tipo = :tipo")
    Integer findMaxProgressivo(@Param("anno") int anno, @Param("tipo") CorrespondenceType tipo);
}
