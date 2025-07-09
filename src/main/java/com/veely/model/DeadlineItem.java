package com.veely.model;


import java.time.LocalDate;

import com.veely.entity.EmploymentAddress;

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
        String type,
        LocalDate startDate,
        JobQualification jobTitle,
        EmploymentAddress workplace,
        JobRole jobRole
) {}