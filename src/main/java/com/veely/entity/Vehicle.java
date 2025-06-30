package com.veely.entity;

import com.veely.model.*;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

/**
 * Veicolo aziendale (auto o camion), sia di proprietà sia in leasing.
 * Campi ereditati dal tuo modello precedente: canoni, km contrattuali,
 * fuel-card, telepass, ecc.:contentReference[oaicite:2]{index=2}
 */
@Entity
@Table(name = "vehicles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Vehicle {

    // -----------------------------------
    // Identificativi
    // -----------------------------------
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Targa – univoca. */
    @Column(nullable = false, unique = true, length = 20)
    private String plate;

    /** Numero telaio (VIN). */
    @Column(unique = true, length = 50)
    private String chassisNumber;

    // -----------------------------------
    // Dati descrittivi
    // -----------------------------------
    private String brand;         // Marca
    private String model;         // Modello
    private String series;        // Allestimento / serie
    private Integer year;         // Anno di immatricolazione (solo cifra)
    private String color;

    @Enumerated(EnumType.STRING)
    private VehicleType type;     // CAR / TRUCK

    @Enumerated(EnumType.STRING)
    private FuelType fuelType;    // Benzina, Diesel, ...

    @Enumerated(EnumType.STRING)
    private OwnershipType ownership; // OWNED / LEASED

    // -----------------------------------
    // Contratto di leasing / proprietà
    // -----------------------------------
    private LocalDate registrationDate;   // Data immatricolazione
    private LocalDate contractStartDate;  // Inizio leasing
    private LocalDate contractEndDate;    // Fine leasing
    private Integer contractDuration;     // Durata (mesi)
    private Integer contractualKm;        // Km previsti dal contratto

    private BigDecimal financialFee;      // Canone finanziario
    private BigDecimal assistanceFee;     // Canone assistenza
    private BigDecimal totalFee;          // Canone complessivo

    private BigDecimal annualFringeBenefit;   // Fringe benefit annuo
    private BigDecimal monthlyFringeBenefit;  // Fringe benefit mensile

    // -----------------------------------
    // Status operativi
    // -----------------------------------
    @Enumerated(EnumType.STRING)
    private VehicleStatus status = VehicleStatus.IN_SERVICE;

    private Integer currentMileage;       // Km attuali
    private String supplier;              // Società di leasing / vendor

    // -----------------------------------
    // Accessori gestionali
    // -----------------------------------
    @Column(unique = true)
    private String fuelCard;              // Carta carburante
    private LocalDate fuelCardExpiryDate;

    private String telepass;              // Telepass associato
    private String imagePath;             // Path immagine del veicolo

    // -----------------------------------
    // Relazioni
    // -----------------------------------
    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Assignment> assignments;

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Document> documents;
}