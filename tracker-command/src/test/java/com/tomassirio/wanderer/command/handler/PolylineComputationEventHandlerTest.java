package com.tomassirio.wanderer.command.handler;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.tomassirio.wanderer.command.event.TripUpdatedEvent;
import com.tomassirio.wanderer.command.service.PolylineService;
import com.tomassirio.wanderer.commons.domain.GeoLocation;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PolylineComputationEventHandlerTest {

    @Mock private PolylineService polylineService;

    @InjectMocks private PolylineComputationEventHandler handler;

    @Test
    void handleTripUpdated_shouldCallAppendSegment() {
        // Given
        UUID tripId = UUID.randomUUID();
        TripUpdatedEvent event =
                TripUpdatedEvent.builder()
                        .tripUpdateId(UUID.randomUUID())
                        .tripId(tripId)
                        .location(GeoLocation.builder().lat(42.0).lon(-8.0).build())
                        .batteryLevel(85)
                        .message("Test")
                        .timestamp(Instant.now())
                        .build();

        // When
        handler.handleTripUpdated(event);

        // Then
        verify(polylineService).appendSegment(tripId);
    }

    @Test
    void handleTripUpdated_whenPolylineServiceThrows_shouldNotPropagate() {
        // Given
        UUID tripId = UUID.randomUUID();
        TripUpdatedEvent event =
                TripUpdatedEvent.builder()
                        .tripUpdateId(UUID.randomUUID())
                        .tripId(tripId)
                        .location(GeoLocation.builder().lat(42.0).lon(-8.0).build())
                        .timestamp(Instant.now())
                        .build();

        doThrow(new RuntimeException("API failure")).when(polylineService).appendSegment(tripId);

        // When — should not throw
        handler.handleTripUpdated(event);

        // Then
        verify(polylineService).appendSegment(tripId);
    }
}
