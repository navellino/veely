package com.veely.model;

public enum AssignmentStatus {
    ASSIGNED("Assegnato"),
    RETURNED("Restituito"),
    BOOKED("Prenotato");

    private final String displayName;

    AssignmentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
