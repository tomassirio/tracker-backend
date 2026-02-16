package com.tomassirio.wanderer.command.handler;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tomassirio.wanderer.command.event.TripDeletedEvent;
import com.tomassirio.wanderer.commons.domain.Trip;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TripDeletedEventHandlerTest {

    @Mock private EntityManager entityManager;

    @InjectMocks private TripDeletedEventHandler handler;

    @Test
    void handle_whenEventReceived_shouldDeleteTrip() {
        // Given
        UUID tripId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        Trip trip = Trip.builder().id(tripId).build();
        TripDeletedEvent event = TripDeletedEvent.builder().tripId(tripId).ownerId(ownerId).build();

        when(entityManager.find(Trip.class, tripId)).thenReturn(trip);

        // When
        handler.handle(event);

        // Then
        verify(entityManager).remove(trip);
    }
}
