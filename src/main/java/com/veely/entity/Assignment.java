package com.veely.entity;

import com.veely.model.*;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "assignments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Assignment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Rapporto di lavoro a cui questa assegnazione è legata */
    @ManyToOne(optional = false)
    private Employment employment;

    @ManyToOne(optional = false)
    private Vehicle vehicle;

    @Enumerated(EnumType.STRING)
    private AssignmentType type; // LONG_TERM vs SHORT_TERM

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime startTime;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    private AssignmentStatus status;
    
    /** Nota operativa per assegnazioni brevi (es. motivo utilizzo). */
    @Column(length = 255)
    private String note;
}

