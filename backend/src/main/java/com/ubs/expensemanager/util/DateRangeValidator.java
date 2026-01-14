package com.ubs.expensemanager.util;

import java.time.LocalDate;

/**
 * Utility class for validating date ranges.
 * 
 * <p>Provides reusable validation logic that can be used across controllers.</p>
 */
public class DateRangeValidator {
    /**
     * Validates that a date range is valid.
     * 
     * @param startDate the start date
     * @param endDate the end date
     * @throws IllegalArgumentException if validation fails
     */
    public static void validate(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }
        
        LocalDate today = LocalDate.now();
        if (startDate.isAfter(today)) {
            throw new IllegalArgumentException("Start date cannot be in the future");
        }
        if (endDate.isAfter(today)) {
            throw new IllegalArgumentException("End date cannot be in the future");
        }
    }

    /**
     * Validates that a date range is within the same month and year.
     * Only used for department budget reports that require same-month validation.
     * 
     * @param startDate the start date
     * @param endDate the end date
     * @throws IllegalArgumentException if dates are not in the same month/year
     */
    public static void validateSameMonthOrSingleDay(LocalDate startDate, LocalDate endDate) {
        // First validate basic rules
        validate(startDate, endDate);
        
        // If it's a single day, it's always valid
        if (startDate.equals(endDate)) {
            return;
        }
        
        // For date ranges, must be in the same month and year
        if (startDate.getYear() != endDate.getYear() || startDate.getMonth() != endDate.getMonth()) {
            throw new IllegalArgumentException("Date range must be within the same month and year, or a single day");
        }
    }
}
