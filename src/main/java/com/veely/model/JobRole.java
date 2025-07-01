package com.veely.model;

/**
 * Mansioni principali all'interno dell'azienda.
 */
public enum JobRole {
    LABORER("Manovale"),
    CLERK("Vecchio"),
    TECHNICIAN("Vecchio"),
    BRICKLAYER("Muratore"),
    ACCOUNTANT("Contabile"),
    SURVEYOR("Geometra"),
    MANAGER("Responsabile");

    private final String displayName;

    JobRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
