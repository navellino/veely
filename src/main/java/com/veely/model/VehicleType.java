package com.veely.model;

public enum VehicleType {
    CAR("Auto"),
    TRUCK("Camion");

    private final String displayName;

    VehicleType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
