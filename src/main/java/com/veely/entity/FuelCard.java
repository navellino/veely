package com.veely.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Scheda carburante associata a un veicolo o a un dipendente.
 */
@Entity
@Table(name = "fuel_cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FuelCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Numero della carta carburante */
    @Column(nullable = false, unique = true)
    private String cardNumber;

    /** Data di scadenza della carta */
    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    /** Fornitore della carta carburante */
    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    /** Dipendente assegnatario della carta */
    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    /** Veicolo associato alla carta */
    @ManyToOne
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    /** Plafond mensile o totale della carta */
    private BigDecimal plafond;
}
