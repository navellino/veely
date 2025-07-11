package com.veely.entity;

import com.veely.model.CorrespondenceType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@Entity
@Table(name = "correspondence")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Correspondence {
    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private int progressivo;

    @Column(nullable = false)
    private int anno;

    @Enumerated(EnumType.STRING)
    @Column(length = 1, nullable = false)
    private CorrespondenceType tipo;

    private String descrizione;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate data;
    
    @Column(nullable = false)
    private String sender;
    
    private String recipient;

    private String notes;
    
    @OneToOne(mappedBy = "correspondence", cascade = CascadeType.ALL, orphanRemoval = true)
    private Document document;
    
}