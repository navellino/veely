package com.veely.entity;

import com.veely.model.CcnlType;
import com.veely.model.ContractType;
import com.veely.model.EmploymentStatus;
import com.veely.model.JobQualification;
import com.veely.model.JobRole;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import org.springframework.format.annotation.DateTimeFormat;

/**
 * Rapporto di lavoro associato ad una persona.
 * Contiene la matricola, i dati contrattuali e le assegnazioni veicoli.
 */
@Entity
@Table(name = "employments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Employment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Persona a cui appartiene questo rapporto. */
    @ManyToOne(optional = false)
    private Employee employee;

    /** Matricola aziendale (univoca all’interno dell’organizzazione). */
    @Column(nullable = false, unique = true, length = 10)
    private String matricola;

    // ------ dati contrattuali ------
    @Enumerated(EnumType.STRING)
    private ContractType contractType;   // Tempo indeterminato / determinato

    private String branch;               // Filiale
    private String department;           // Reparto / ufficio
    
    @Enumerated(EnumType.STRING)
    private JobQualification jobTitle;             // Qualifica
    
    private String contractLevel;        // Livello contrattuale

    @Enumerated(EnumType.STRING)
    private CcnlType ccnl;               // CCNL applicato
    
    @Enumerated(EnumType.STRING)
    private JobRole jobRole;             // Mansione predefinita

    private BigDecimal salary;           // Retribuzione base

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;
    
    @Column(name = "job_description")
    private String jobDescription;                // qualifica / mansione
    
    @Embedded
    private EmploymentAddress workplace;

    @Enumerated(EnumType.STRING)
    private EmploymentStatus status;

    // ------ relazioni ------
    @OneToMany(mappedBy = "employment", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Assignment> assignments;

    @OneToMany(mappedBy = "employment", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Document> employmentDocuments;
}
