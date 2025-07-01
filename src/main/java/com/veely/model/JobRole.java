package com.veely.model;

/**
 * Mansioni principali all'interno dell'azienda.
 */
public enum JobRole {
    WORKER("Operaio"),
    CLERK("Impiegato"),
    TECHNICIAN("Tecnico"),
    MANAGER("Responsabile");

    private final String displayName;

    JobRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
