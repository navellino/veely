package com.veely.model;

public enum DocumentType {
    // ---- VEICOLO ----
    VEHICLE_REGISTRATION("Libretto circolazione"),
    INSURANCE("Assicurazione"),
    MAINTENANCE("Manutenzioni"),
    VEHICLE_IMAGE("Immagine veicolo"),
    LEASE_CONTRACT("Contratto di leasing"),
    SPACE("--------------------"),

    // ---- RAPPORTO DI LAVORO ----
    EMPLOYMENT_CONTRACT("Contratto di lavoro"),
    PAY_RISE("Aumento retribuzione"),
    DISCIPLINARY_LETTER("Lettera di richiamo"),

    // ---- PERSONA ----
    ASSIGNMENT_LETTER("Lettera di assegnazione veicolo"),
    IDENTITY_DOCUMENT("Documento identità"),
    IDENTITY_PHOTO("Foto Profilo"),
    ASSIGNMENT_PHOTO("Foto veicolo durante assegnazione"),
    ASSIGNMENT_REPORT("Report riconsegna / danni"),
    OTHER("Altro");

    private final String displayName;

    DocumentType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
