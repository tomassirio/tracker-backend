package com.tomassirio.wanderer.command.handler.websocket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.tomassirio.wanderer.command.event.TripCreatedEvent;
import com.tomassirio.wanderer.command.websocket.WebSocketEventService;
import com.tomassirio.wanderer.command.websocket.WebSocketEventType;
import com.tomassirio.wanderer.command.websocket.payload.TripLifecyclePayload;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TripCreatedEventWebSocketHandlerTest {

    @Mock private WebSocketEventService webSocketEventService;

    @InjectMocks private TripCreatedEventWebSocketHandler handler;

    @Test
    void handle_whenEventReceived_shouldBroadcastToTrip() {
        // Given
        UUID tripId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        TripCreatedEvent event =
                TripCreatedEvent.builder()
                        .tripId(tripId)
                        .tripName("Camino de Santiago")
                        .ownerId(ownerId)
                        .visibility("PUBLIC")
                        .creationTimestamp(Instant.now())
                        .build();

        // When
        handler.handle(event);

        // Then
        ArgumentCaptor<TripLifecyclePayload> payloadCaptor =
                ArgumentCaptor.forClass(TripLifecyclePayload.class);
        verify(webSocketEventService)
                .broadcastToTrip(
                        eq(tripId), eq(WebSocketEventType.TRIP_CREATED), payloadCaptor.capture());

        TripLifecyclePayload payload = payloadCaptor.getValue();
        assertThat(payload.getTripId()).isEqualTo(tripId);
        assertThat(payload.getTripName()).isEqualTo("Camino de Santiago");
        assertThat(payload.getOwnerId()).isEqualTo(ownerId);
        assertThat(payload.getVisibility()).isEqualTo("PUBLIC");
    }
}
