package com.tomassirio.wanderer.command.event.handler.websocket;

import static org.mockito.Mockito.verify;

import com.tomassirio.wanderer.command.event.TripStatusChangedEvent;
import com.tomassirio.wanderer.command.websocket.WebSocketEventService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TripStatusChangedEventWebSocketHandlerTest {

    @Mock private WebSocketEventService webSocketEventService;

    @InjectMocks private TripStatusChangedEventWebSocketHandler handler;

    @Test
    void handle_whenEventReceived_shouldBroadcastTripStatusChanged() {
        // Given
        UUID tripId = UUID.randomUUID();
        TripStatusChangedEvent event =
                TripStatusChangedEvent.builder()
                        .tripId(tripId)
                        .previousStatus("CREATED")
                        .newStatus("IN_PROGRESS")
                        .build();

        // When
        handler.handle(event);

        // Then
        verify(webSocketEventService).broadcastTripStatusChanged(tripId, "IN_PROGRESS", "CREATED");
    }

    @Test
    void handle_whenPreviousStatusIsNull_shouldBroadcastWithNullPreviousStatus() {
        // Given
        UUID tripId = UUID.randomUUID();
        TripStatusChangedEvent event =
                TripStatusChangedEvent.builder()
                        .tripId(tripId)
                        .previousStatus(null)
                        .newStatus("CREATED")
                        .build();

        // When
        handler.handle(event);

        // Then
        verify(webSocketEventService).broadcastTripStatusChanged(tripId, "CREATED", null);
    }
}
