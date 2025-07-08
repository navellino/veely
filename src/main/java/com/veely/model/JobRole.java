package com.veely.model;

/**
 * Mansioni principali all'interno dell'azienda.
 */
public enum JobRole {
	LEGAL_ADMIN("Amministratore"),
    LABORER("Manovale"),
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
