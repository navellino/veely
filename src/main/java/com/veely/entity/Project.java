package com.veely.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Codice identificativo della commessa */
    @Column(nullable = false, unique = true)
    private String code;
    
    /** Descrizione o denominazione della commessa */
    @Column(nullable = false)
    private String name;
    
    /** Codice Identificativo Gara (opzionale) */
    private String cig;

    /** Codice Unico di Progetto (opzionale) */
    private String cup;
}
