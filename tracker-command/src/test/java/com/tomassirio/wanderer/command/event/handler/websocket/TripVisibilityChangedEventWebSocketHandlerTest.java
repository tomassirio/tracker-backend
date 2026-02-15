package com.tomassirio.wanderer.command.event.handler.websocket;

import static org.mockito.Mockito.verify;

import com.tomassirio.wanderer.command.event.TripVisibilityChangedEvent;
import com.tomassirio.wanderer.command.websocket.WebSocketEventService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TripVisibilityChangedEventWebSocketHandlerTest {

    @Mock private WebSocketEventService webSocketEventService;

    @InjectMocks private TripVisibilityChangedEventWebSocketHandler handler;

    @Test
    void handle_whenEventReceived_shouldBroadcastTripVisibilityChanged() {
        // Given
        UUID tripId = UUID.randomUUID();
        TripVisibilityChangedEvent event =
                TripVisibilityChangedEvent.builder()
                        .tripId(tripId)
                        .previousVisibility("PUBLIC")
                        .newVisibility("PRIVATE")
                        .build();

        // When
        handler.handle(event);

        // Then
        verify(webSocketEventService).broadcastTripVisibilityChanged(tripId, "PRIVATE", "PUBLIC");
    }
}
