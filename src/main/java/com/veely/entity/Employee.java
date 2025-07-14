package com.veely.entity;


import com.veely.model.DocumentType;
import com.veely.model.EducationLevel;
import com.veely.model.FullAddress;
import com.veely.model.Gender;
import com.veely.model.MaritalStatus;
import com.veely.model.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;

import org.springframework.format.annotation.DateTimeFormat;

/**
 * Entity Employee: dati anagrafici, contatti, login, stato civile, 
 * titolo di studio, PEC e indirizzo di residenza completo.
 */
@Entity
@Table(name = "employees")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Employee {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- Anagrafica ---
    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate birthDate;
    private String birthPlace;
    
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(nullable = false, unique = true, length = 16)
    private String fiscalCode;
    
    // --- Contatti ---
    private String phone;
    private String mobile;
    @Column(nullable = true)
    private String iban;

    @Column(nullable = false, unique = true)
    private String email;

    /** Password cifrata (BCrypt). */
    @Column(nullable = false)
    private String password;

    // --- Ruolo Spring Security ---
    @Enumerated(EnumType.STRING)
    private UserRole role;

    /** Stato civile ← enum con displayName */
    @Enumerated(EnumType.STRING)
    @Column(name = "marital_status")
    private MaritalStatus maritalStatus;

    /** Titolo di studio ← enum con displayName */
    @Enumerated(EnumType.STRING)
    @Column(name = "education_level")
    private EducationLevel educationLevel;

    /** Indirizzo di residenza completo */
    @Embedded
    private FullAddress residenceAddress;

    /** PEC (facoltativa) */
    private String pec;

    // --- Relazioni ---
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Employment> employments;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Document> personalDocuments;
    
    @Transient
    public Document getProfilePhoto() {
        return personalDocuments.stream()
            .filter(doc -> doc.getType() == DocumentType.IDENTITY_PHOTO)
            .findFirst()
            .orElse(null);
    }
    
    @Transient
    public Integer getAge() {
        return birthDate == null ? null
                : java.time.Period.between(birthDate, java.time.LocalDate.now()).getYears();
    }
    
}
