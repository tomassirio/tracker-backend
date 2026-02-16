package com.tomassirio.wanderer.command.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tomassirio.wanderer.command.event.TripUpdatedEvent;
import com.tomassirio.wanderer.command.service.AchievementCalculationService;
import com.tomassirio.wanderer.commons.domain.GeoLocation;
import com.tomassirio.wanderer.commons.domain.Trip;
import com.tomassirio.wanderer.commons.domain.TripUpdate;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TripUpdatedEventHandlerTest {

    @Mock private EntityManager entityManager;

    @Mock private AchievementCalculationService achievementCalculationService;

    @InjectMocks private TripUpdatedEventHandler handler;

    @Test
    void handle_shouldPersistTripUpdate() {
        // Given
        UUID tripId = UUID.randomUUID();
        UUID tripUpdateId = UUID.randomUUID();
        GeoLocation location = GeoLocation.builder().lat(42.8805).lon(-8.5457).build();
        Instant timestamp = Instant.now();

        Trip trip = Trip.builder().id(tripId).name("Camino").build();

        TripUpdatedEvent event =
                TripUpdatedEvent.builder()
                        .tripUpdateId(tripUpdateId)
                        .tripId(tripId)
                        .location(location)
                        .batteryLevel(85)
                        .message("Arrived at Santiago!")
                        .timestamp(timestamp)
                        .build();

        when(entityManager.getReference(Trip.class, tripId)).thenReturn(trip);

        // When
        handler.handle(event);

        // Then
        ArgumentCaptor<TripUpdate> captor = ArgumentCaptor.forClass(TripUpdate.class);
        verify(entityManager).persist(captor.capture());

        TripUpdate saved = captor.getValue();
        assertThat(saved.getId()).isEqualTo(tripUpdateId);
        assertThat(saved.getTrip()).isEqualTo(trip);
        assertThat(saved.getLocation()).isEqualTo(location);
        assertThat(saved.getBattery()).isEqualTo(85);
        assertThat(saved.getMessage()).isEqualTo("Arrived at Santiago!");
        assertThat(saved.getTimestamp()).isEqualTo(timestamp);

        // Verify achievement calculation was triggered
        verify(achievementCalculationService).checkAndUnlockAchievements(tripId);
    }
}
