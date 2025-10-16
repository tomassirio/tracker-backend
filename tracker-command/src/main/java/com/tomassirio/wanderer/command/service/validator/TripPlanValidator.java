package com.tomassirio.wanderer.command.service.validator;

import java.time.LocalDate;
import org.springframework.stereotype.Component;

/**
 * Validator component for trip plan business rules. Handles validation of trip plan data such as
 * dates, locations, etc.
 *
 * @since 0.3.0
 */
@Component
public class TripPlanValidator {

    /**
     * Validates that the end date is after the start date.
     *
     * @param startDate the start date of the trip plan
     * @param endDate the end date of the trip plan
     * @throws IllegalArgumentException if the end date is not after the start date
     */
    public void validateDates(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate) || endDate.isEqual(startDate)) {
            throw new IllegalArgumentException("End date must be after start date");
        }
    }
}
