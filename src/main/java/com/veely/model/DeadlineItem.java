package com.veely.model;


import java.time.LocalDate;

/**
 * Simple DTO representing a deadline entry aggregated across the system.
 */
public record DeadlineItem(
        String category,
        String name,
        LocalDate dueDate,
        Long vehicleId,
        String type
) {}