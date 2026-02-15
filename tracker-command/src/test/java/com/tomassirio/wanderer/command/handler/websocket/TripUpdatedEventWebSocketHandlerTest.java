package com.tomassirio.wanderer.command.handler.websocket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.tomassirio.wanderer.command.event.TripUpdatedEvent;
import com.tomassirio.wanderer.command.websocket.WebSocketEventService;
import com.tomassirio.wanderer.command.websocket.WebSocketEventType;
import com.tomassirio.wanderer.command.websocket.payload.TripUpdatedPayload;
import com.tomassirio.wanderer.commons.domain.GeoLocation;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TripUpdatedEventWebSocketHandlerTest {

    @Mock private WebSocketEventService webSocketEventService;

    @InjectMocks private TripUpdatedEventWebSocketHandler handler;

    @Test
    void handle_whenEventReceived_shouldBroadcastToTrip() {
        // Given
        UUID tripId = UUID.randomUUID();
        GeoLocation location = new GeoLocation(42.8805, -8.5457);
        TripUpdatedEvent event =
                TripUpdatedEvent.builder()
                        .tripId(tripId)
                        .location(location)
                        .batteryLevel(85)
                        .message("Arrived at Santiago!")
                        .build();

        // When
        handler.handle(event);

        // Then
        ArgumentCaptor<TripUpdatedPayload> payloadCaptor =
                ArgumentCaptor.forClass(TripUpdatedPayload.class);
        verify(webSocketEventService)
                .broadcastToTrip(
                        eq(tripId), eq(WebSocketEventType.TRIP_UPDATED), payloadCaptor.capture());

        TripUpdatedPayload payload = payloadCaptor.getValue();
        assertThat(payload.getTripId()).isEqualTo(tripId);
        assertThat(payload.getLatitude()).isEqualTo(42.8805);
        assertThat(payload.getLongitude()).isEqualTo(-8.5457);
        assertThat(payload.getBatteryLevel()).isEqualTo(85);
        assertThat(payload.getMessage()).isEqualTo("Arrived at Santiago!");
    }
}
