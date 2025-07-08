package com.veely.model;

public enum DocumentType {
    // ---- VEICOLO ----
	SPACE1("---- VEICOLO ----"),
    VEHICLE_REGISTRATION("Libretto circolazione"),
    INSURANCE("Assicurazione"),
    MAINTENANCE("Manutenzioni"),
    VEHICLE_IMAGE("Immagine veicolo"),
    LEASE_CONTRACT("Contratto di leasing"),
    
    // ---- RAPPORTO DI LAVORO ----
    SPACE("---- RAPPORTO DI LAVORO ----"),
    EMPLOYMENT_CONTRACT("Contratto di lavoro"),
    PAY_RISE("Aumento retribuzione"),
    DISCIPLINARY_LETTER("Lettera di richiamo"),

    // ---- PERSONA ----
    SPACE2("---- PERSONA ----"),
    IDENTITY_PHOTO("Foto Profilo"),
    IDENTITY_DOCUMENT("Documento identità"),
    FISCAL_CODE("Tessera Sanitaria"),
    SAP("Scheda Anagrafica Professionale"),
    GRADE("Titolo di studio"),
    
    // ---- ASSEGNAZIONI ----
    SPACE3("---- ASSEGNAZIONI ----"),
    ASSIGNMENT_PHOTO("Foto veicolo durante assegnazione"),
    ASSIGNMENT_REPORT("Report riconsegna / danni"),
    ASSIGNMENT_LETTER("Lettera di assegnazione veicolo"),
    
    
    OTHER("Altro");

    private final String displayName;

    DocumentType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
