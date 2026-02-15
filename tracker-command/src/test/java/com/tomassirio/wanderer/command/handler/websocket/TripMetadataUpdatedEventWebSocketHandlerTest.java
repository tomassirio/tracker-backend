package com.tomassirio.wanderer.command.handler.websocket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.tomassirio.wanderer.command.event.TripMetadataUpdatedEvent;
import com.tomassirio.wanderer.command.websocket.WebSocketEventService;
import com.tomassirio.wanderer.command.websocket.WebSocketEventType;
import com.tomassirio.wanderer.command.websocket.payload.TripLifecyclePayload;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TripMetadataUpdatedEventWebSocketHandlerTest {

    @Mock private WebSocketEventService webSocketEventService;

    @InjectMocks private TripMetadataUpdatedEventWebSocketHandler handler;

    @Test
    void handle_whenEventReceived_shouldBroadcastToTrip() {
        // Given
        UUID tripId = UUID.randomUUID();
        TripMetadataUpdatedEvent event =
                TripMetadataUpdatedEvent.builder()
                        .tripId(tripId)
                        .tripName("Updated Camino")
                        .visibility("PRIVATE")
                        .build();

        // When
        handler.handle(event);

        // Then
        ArgumentCaptor<TripLifecyclePayload> payloadCaptor =
                ArgumentCaptor.forClass(TripLifecyclePayload.class);
        verify(webSocketEventService)
                .broadcastToTrip(
                        eq(tripId),
                        eq(WebSocketEventType.TRIP_METADATA_UPDATED),
                        payloadCaptor.capture());

        TripLifecyclePayload payload = payloadCaptor.getValue();
        assertThat(payload.getTripId()).isEqualTo(tripId);
        assertThat(payload.getTripName()).isEqualTo("Updated Camino");
        assertThat(payload.getVisibility()).isEqualTo("PRIVATE");
        assertThat(payload.getOwnerId()).isNull();
    }
}
