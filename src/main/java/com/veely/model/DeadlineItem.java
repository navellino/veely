package com.veely.model;


import java.time.LocalDate;

/**
 * Simple DTO representing a deadline entry aggregated across the system.
 */
public record DeadlineItem(
        String category,
        String name,
        String brand,
        String model,
        String series,
        LocalDate dueDate,
        Long refId,
        String type
) {}