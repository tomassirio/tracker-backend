package com.tomassirio.wanderer.command.event.handler.websocket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.tomassirio.wanderer.command.event.TripUpdatedEvent;
import com.tomassirio.wanderer.command.websocket.TripUpdatedPayload;
import com.tomassirio.wanderer.command.websocket.WebSocketEventService;
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
    void handle_whenEventReceived_shouldBroadcastTripUpdated() {
        // Given
        UUID tripId = UUID.randomUUID();
        TripUpdatedEvent event =
                TripUpdatedEvent.builder()
                        .tripId(tripId)
                        .latitude(42.8805)
                        .longitude(-8.5457)
                        .batteryLevel(85)
                        .message("Arrived at Santiago!")
                        .build();

        // When
        handler.handle(event);

        // Then
        ArgumentCaptor<TripUpdatedPayload> payloadCaptor =
                ArgumentCaptor.forClass(TripUpdatedPayload.class);
        verify(webSocketEventService).broadcastTripUpdated(payloadCaptor.capture());

        TripUpdatedPayload payload = payloadCaptor.getValue();
        assertThat(payload.getTripId()).isEqualTo(tripId);
        assertThat(payload.getLatitude()).isEqualTo(42.8805);
        assertThat(payload.getLongitude()).isEqualTo(-8.5457);
        assertThat(payload.getBatteryLevel()).isEqualTo(85);
        assertThat(payload.getMessage()).isEqualTo("Arrived at Santiago!");
    }
}
