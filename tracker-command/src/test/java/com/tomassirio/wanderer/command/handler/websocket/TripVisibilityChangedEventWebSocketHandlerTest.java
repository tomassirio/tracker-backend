package com.tomassirio.wanderer.command.handler.websocket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.tomassirio.wanderer.command.event.TripVisibilityChangedEvent;
import com.tomassirio.wanderer.command.websocket.WebSocketEventService;
import com.tomassirio.wanderer.command.websocket.WebSocketEventType;
import com.tomassirio.wanderer.command.websocket.payload.TripVisibilityChangedPayload;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TripVisibilityChangedEventWebSocketHandlerTest {

    @Mock private WebSocketEventService webSocketEventService;

    @InjectMocks private TripVisibilityChangedEventWebSocketHandler handler;

    @Test
    void handle_whenEventReceived_shouldBroadcastToTrip() {
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
        ArgumentCaptor<TripVisibilityChangedPayload> payloadCaptor =
                ArgumentCaptor.forClass(TripVisibilityChangedPayload.class);
        verify(webSocketEventService)
                .broadcastToTrip(
                        eq(tripId),
                        eq(WebSocketEventType.TRIP_VISIBILITY_CHANGED),
                        payloadCaptor.capture());

        TripVisibilityChangedPayload payload = payloadCaptor.getValue();
        assertThat(payload.getTripId()).isEqualTo(tripId);
        assertThat(payload.getNewVisibility()).isEqualTo("PRIVATE");
        assertThat(payload.getPreviousVisibility()).isEqualTo("PUBLIC");
    }
}
