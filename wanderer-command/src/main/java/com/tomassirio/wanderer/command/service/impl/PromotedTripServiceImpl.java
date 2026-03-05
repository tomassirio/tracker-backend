package com.tomassirio.wanderer.command.service.impl;

import com.tomassirio.wanderer.command.event.DonationLinkUpdatedEvent;
import com.tomassirio.wanderer.command.event.TripPromotedEvent;
import com.tomassirio.wanderer.command.event.TripUnpromotedEvent;
import com.tomassirio.wanderer.command.repository.PromotedTripRepository;
import com.tomassirio.wanderer.command.repository.TripRepository;
import com.tomassirio.wanderer.command.service.PromotedTripService;
import com.tomassirio.wanderer.commons.domain.PromotedTrip;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the PromotedTripService interface.
 *
 * <p>This service manages promoted trip operations following CQRS patterns. All write operations
 * publish events that are handled asynchronously by event handlers.
 *
 * @author tomassirio
 * @since 0.5.0
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PromotedTripServiceImpl implements PromotedTripService {

    private final TripRepository tripRepository;
    private final PromotedTripRepository promotedTripRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public UUID promoteTrip(UUID adminId, UUID tripId, String donationLink) {
        // Validate trip exists
        tripRepository
                .findById(tripId)
                .orElseThrow(() -> new EntityNotFoundException("Trip not found"));

        // Check if trip is already promoted
        if (promotedTripRepository.existsByTripId(tripId)) {
            throw new IllegalStateException("Trip is already promoted");
        }

        // Pre-generate ID and timestamp for promoted trip
        UUID promotedTripId = UUID.randomUUID();
        Instant promotedAt = Instant.now();

        // Publish event - persistence handler will write to DB
        eventPublisher.publishEvent(
                TripPromotedEvent.builder()
                        .id(promotedTripId)
                        .tripId(tripId)
                        .donationLink(donationLink)
                        .promotedBy(adminId)
                        .promotedAt(promotedAt)
                        .build());

        return promotedTripId;
    }

    @Override
    public void unpromoteTrip(UUID adminId, UUID tripId) {
        // Validate trip exists
        tripRepository
                .findById(tripId)
                .orElseThrow(() -> new EntityNotFoundException("Trip not found"));

        // Check if trip is promoted
        if (!promotedTripRepository.existsByTripId(tripId)) {
            throw new EntityNotFoundException("Trip is not promoted");
        }

        // Publish event - persistence handler will delete from DB
        eventPublisher.publishEvent(
                TripUnpromotedEvent.builder().tripId(tripId).unpromotedBy(adminId).build());
    }

    @Override
    public UUID updatePromotedTripDonationLink(UUID adminId, UUID tripId, String donationLink) {
        // Validate trip exists
        tripRepository
                .findById(tripId)
                .orElseThrow(() -> new EntityNotFoundException("Trip not found"));

        // Get the promoted trip
        PromotedTrip promotedTrip =
                promotedTripRepository
                        .findByTripId(tripId)
                        .orElseThrow(() -> new EntityNotFoundException("Trip is not promoted"));

        // Publish event - persistence handler will update in DB
        eventPublisher.publishEvent(
                DonationLinkUpdatedEvent.builder()
                        .promotedTripId(promotedTrip.getId())
                        .tripId(tripId)
                        .donationLink(donationLink)
                        .updatedBy(adminId)
                        .build());

        return promotedTrip.getId();
    }
}
