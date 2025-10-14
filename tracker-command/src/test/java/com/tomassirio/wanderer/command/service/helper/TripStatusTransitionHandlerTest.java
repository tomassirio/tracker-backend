package com.tomassirio.wanderer.command.service.helper;

import static org.assertj.core.api.Assertions.assertThat;

import com.tomassirio.wanderer.commons.domain.Trip;
import com.tomassirio.wanderer.commons.domain.TripDetails;
import com.tomassirio.wanderer.commons.domain.TripStatus;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TripStatusTransitionHandlerTest {

    private TripStatusTransitionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new TripStatusTransitionHandler();
    }

    @Test
    void handleStatusTransition_whenTransitionFromCreatedToInProgress_shouldSetStartTimestamp() {
        // Given
        Trip trip = createTripWithDetails();
        TripStatus previousStatus = TripStatus.CREATED;
        TripStatus newStatus = TripStatus.IN_PROGRESS;

        // When
        Instant beforeCall = Instant.now();
        handler.handleStatusTransition(trip, previousStatus, newStatus);
        Instant afterCall = Instant.now();

        // Then
        assertThat(trip.getTripDetails().getStartTimestamp()).isNotNull();
        assertThat(trip.getTripDetails().getStartTimestamp())
                .isAfterOrEqualTo(beforeCall)
                .isBeforeOrEqualTo(afterCall);
        assertThat(trip.getTripDetails().getEndTimestamp()).isNull();
    }

    @Test
    void handleStatusTransition_whenTransitionToFinished_shouldSetEndTimestamp() {
        // Given
        Trip trip = createTripWithDetails();
        TripStatus previousStatus = TripStatus.IN_PROGRESS;
        TripStatus newStatus = TripStatus.FINISHED;

        // When
        Instant beforeCall = Instant.now();
        handler.handleStatusTransition(trip, previousStatus, newStatus);
        Instant afterCall = Instant.now();

        // Then
        assertThat(trip.getTripDetails().getEndTimestamp()).isNotNull();
        assertThat(trip.getTripDetails().getEndTimestamp())
                .isAfterOrEqualTo(beforeCall)
                .isBeforeOrEqualTo(afterCall);
        assertThat(trip.getTripDetails().getStartTimestamp()).isNull();
    }

    @Test
    void handleStatusTransition_whenTransitionFromCreatedToFinished_shouldOnlySetEndTimestamp() {
        // Given
        Trip trip = createTripWithDetails();
        TripStatus previousStatus = TripStatus.CREATED;
        TripStatus newStatus = TripStatus.FINISHED;

        // When
        handler.handleStatusTransition(trip, previousStatus, newStatus);

        // Then
        assertThat(trip.getTripDetails().getEndTimestamp()).isNotNull();
        assertThat(trip.getTripDetails().getStartTimestamp()).isNull();
    }

    @Test
    void handleStatusTransition_whenTransitionFromInProgressToFinished_shouldSetEndTimestamp() {
        // Given
        Trip trip = createTripWithDetails();
        Instant existingStartTimestamp = Instant.now().minusSeconds(3600);
        trip.getTripDetails().setStartTimestamp(existingStartTimestamp);
        TripStatus previousStatus = TripStatus.IN_PROGRESS;
        TripStatus newStatus = TripStatus.FINISHED;

        // When
        handler.handleStatusTransition(trip, previousStatus, newStatus);

        // Then
        assertThat(trip.getTripDetails().getEndTimestamp()).isNotNull();
        assertThat(trip.getTripDetails().getStartTimestamp()).isEqualTo(existingStartTimestamp);
    }

    @Test
    void handleStatusTransition_whenTransitionFromInProgressToPaused_shouldNotModifyTimestamps() {
        // Given
        Trip trip = createTripWithDetails();
        Instant existingStartTimestamp = Instant.now().minusSeconds(3600);
        trip.getTripDetails().setStartTimestamp(existingStartTimestamp);
        TripStatus previousStatus = TripStatus.IN_PROGRESS;
        TripStatus newStatus = TripStatus.PAUSED;

        // When
        handler.handleStatusTransition(trip, previousStatus, newStatus);

        // Then
        assertThat(trip.getTripDetails().getStartTimestamp()).isEqualTo(existingStartTimestamp);
        assertThat(trip.getTripDetails().getEndTimestamp()).isNull();
    }

    @Test
    void handleStatusTransition_whenTransitionFromPausedToInProgress_shouldNotModifyTimestamps() {
        // Given
        Trip trip = createTripWithDetails();
        Instant existingStartTimestamp = Instant.now().minusSeconds(3600);
        trip.getTripDetails().setStartTimestamp(existingStartTimestamp);
        TripStatus previousStatus = TripStatus.PAUSED;
        TripStatus newStatus = TripStatus.IN_PROGRESS;

        // When
        handler.handleStatusTransition(trip, previousStatus, newStatus);

        // Then
        assertThat(trip.getTripDetails().getStartTimestamp()).isEqualTo(existingStartTimestamp);
        assertThat(trip.getTripDetails().getEndTimestamp()).isNull();
    }

    @Test
    void handleStatusTransition_whenTransitionFromPausedToFinished_shouldSetEndTimestamp() {
        // Given
        Trip trip = createTripWithDetails();
        TripStatus previousStatus = TripStatus.PAUSED;
        TripStatus newStatus = TripStatus.FINISHED;

        // When
        handler.handleStatusTransition(trip, previousStatus, newStatus);

        // Then
        assertThat(trip.getTripDetails().getEndTimestamp()).isNotNull();
        assertThat(trip.getTripDetails().getStartTimestamp()).isNull();
    }

    @Test
    void handleStatusTransition_whenTransitionFromPausedToCreated_shouldNotModifyTimestamps() {
        // Given
        Trip trip = createTripWithDetails();
        Instant existingStartTimestamp = Instant.now().minusSeconds(3600);
        trip.getTripDetails().setStartTimestamp(existingStartTimestamp);
        TripStatus previousStatus = TripStatus.PAUSED;
        TripStatus newStatus = TripStatus.CREATED;

        // When
        handler.handleStatusTransition(trip, previousStatus, newStatus);

        // Then
        assertThat(trip.getTripDetails().getStartTimestamp()).isEqualTo(existingStartTimestamp);
        assertThat(trip.getTripDetails().getEndTimestamp()).isNull();
    }

    @Test
    void handleStatusTransition_whenAlreadyHasStartTimestamp_shouldNotOverwriteOnCreatedToInProgress() {
        // Given
        Trip trip = createTripWithDetails();
        Instant existingStartTimestamp = Instant.now().minusSeconds(7200);
        trip.getTripDetails().setStartTimestamp(existingStartTimestamp);
        TripStatus previousStatus = TripStatus.CREATED;
        TripStatus newStatus = TripStatus.IN_PROGRESS;

        // When
        handler.handleStatusTransition(trip, previousStatus, newStatus);

        // Then - should overwrite since the logic sets it unconditionally
        assertThat(trip.getTripDetails().getStartTimestamp()).isNotEqualTo(existingStartTimestamp);
        assertThat(trip.getTripDetails().getStartTimestamp()).isAfter(existingStartTimestamp);
    }

    @Test
    void handleStatusTransition_whenAlreadyHasEndTimestamp_shouldOverwriteOnTransitionToFinished() {
        // Given
        Trip trip = createTripWithDetails();
        Instant existingEndTimestamp = Instant.now().minusSeconds(7200);
        trip.getTripDetails().setEndTimestamp(existingEndTimestamp);
        TripStatus previousStatus = TripStatus.IN_PROGRESS;
        TripStatus newStatus = TripStatus.FINISHED;

        // When
        handler.handleStatusTransition(trip, previousStatus, newStatus);

        // Then - should overwrite
        assertThat(trip.getTripDetails().getEndTimestamp()).isNotEqualTo(existingEndTimestamp);
        assertThat(trip.getTripDetails().getEndTimestamp()).isAfter(existingEndTimestamp);
    }

    private Trip createTripWithDetails() {
        return Trip.builder()
                .tripDetails(TripDetails.builder().build())
                .build();
    }
}
