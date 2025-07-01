package com.veely.model;

public enum Gender {
    MALE("Maschio"),
    FEMALE("Femmina"),
    OTHER("Altro");

    private final String displayName;

    Gender(String displayName) { this.displayName = displayName; }

    public String getDisplayName() { return displayName; }
}
