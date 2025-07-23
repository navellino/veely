package com.veely.entity;

import com.veely.model.*;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "documents")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Document {

	@Id @GeneratedValue
    private Long id;

    @ManyToOne @JoinColumn(name="employee_id")
    private Employee employee;

    @ManyToOne @JoinColumn(name="employment_id")
    private Employment employment;

    @ManyToOne @JoinColumn(name="vehicle_id")
    private Vehicle vehicle;

    @ManyToOne @JoinColumn(name="assignment_id")
    private Assignment assignment;
    
    @ManyToOne
    @JoinColumn(name="expense_item_id")
    private ExpenseItem expenseItem;

    @OneToOne
    @JoinColumn(name="correspondence_id", unique = true)
    private Correspondence correspondence;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 32, nullable = false)
    private DocumentType type;

    private String path;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate issueDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate expiryDate;
}
