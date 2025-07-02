package com.veely.entity;

import com.veely.model.FullAddress;
import jakarta.persistence.*;
import lombok.*;

/**
 * Fornitore/Dealer del veicolo. Può essere la società di leasing
 * o il concessionario dal quale è stato acquistato il mezzo.
 */
@Entity
@Table(name = "suppliers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Denominazione o ragione sociale */
    @Column(nullable = false)
    private String name;

    /** Partita IVA o codice fiscale */
    private String vatNumber;
    
    /* Contatto commerciale */
    private String contatto;

    private String phone;
    private String email;

    @Embedded
    private FullAddress address;
}

