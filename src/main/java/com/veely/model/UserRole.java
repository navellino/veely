package com.veely.model;

public enum UserRole {
    ADMIN("Administrator"),
    MANAGER("Fleet Manager"),
    USER("User");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}