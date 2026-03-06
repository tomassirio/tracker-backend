package com.tomassirio.wanderer.command.service;

import java.time.Instant;
import java.util.UUID;

/**
 * Service interface for managing promoted trip operations in the command side of the CQRS
 * architecture.
 *
 * <p>This service handles all write operations (promote, unpromote, update donation link) for
 * promoted trips. Only administrators can perform these operations.
 *
 * @author tomassirio
 * @since 0.5.0
 */
public interface PromotedTripService {

    /**
     * Promotes a trip. Only administrators can promote trips.
     *
     * @param adminId the UUID of the admin making the request
     * @param tripId the UUID of the trip to promote
     * @param donationLink optional donation link for the promoted trip
     * @param isPreAnnounced whether this trip is pre-announced to display a countdown
     * @param countdownStartDate the date from which the countdown should start; required when
     *     isPreAnnounced is true
     * @return the UUID of the promoted trip record
     * @throws jakarta.persistence.EntityNotFoundException if no trip exists with the given ID
     * @throws IllegalStateException if the trip is already promoted
     * @throws IllegalArgumentException if isPreAnnounced is true but countdownStartDate is null
     */
    UUID promoteTrip(
            UUID adminId,
            UUID tripId,
            String donationLink,
            boolean isPreAnnounced,
            Instant countdownStartDate);

    /**
     * Unpromotes a trip. Only administrators can unpromote trips.
     *
     * @param adminId the UUID of the admin making the request
     * @param tripId the UUID of the trip to unpromote
     * @throws jakarta.persistence.EntityNotFoundException if no trip exists with the given ID or
     *     trip is not promoted
     */
    void unpromoteTrip(UUID adminId, UUID tripId);

    /**
     * Updates the donation link for a promoted trip. Only administrators can update.
     *
     * @param adminId the UUID of the admin making the request
     * @param tripId the UUID of the promoted trip
     * @param donationLink new donation link (can be null to remove)
     * @return the UUID of the promoted trip record
     * @throws jakarta.persistence.EntityNotFoundException if no trip exists with the given ID or
     *     trip is not promoted
     */
    UUID updatePromotedTripDonationLink(UUID adminId, UUID tripId, String donationLink);
}
