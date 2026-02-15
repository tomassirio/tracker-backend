package com.tomassirio.wanderer.command.handler.websocket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.tomassirio.wanderer.command.event.TripStatusChangedEvent;
import com.tomassirio.wanderer.command.websocket.WebSocketEventService;
import com.tomassirio.wanderer.command.websocket.WebSocketEventType;
import com.tomassirio.wanderer.command.websocket.payload.TripStatusChangedPayload;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TripStatusChangedEventWebSocketHandlerTest {

    @Mock private WebSocketEventService webSocketEventService;

    @InjectMocks private TripStatusChangedEventWebSocketHandler handler;

    @Test
    void handle_whenEventReceived_shouldBroadcastToTrip() {
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
        ArgumentCaptor<TripStatusChangedPayload> payloadCaptor =
                ArgumentCaptor.forClass(TripStatusChangedPayload.class);
        verify(webSocketEventService)
                .broadcastToTrip(
                        eq(tripId),
                        eq(WebSocketEventType.TRIP_STATUS_CHANGED),
                        payloadCaptor.capture());

        TripStatusChangedPayload payload = payloadCaptor.getValue();
        assertThat(payload.getTripId()).isEqualTo(tripId);
        assertThat(payload.getNewStatus()).isEqualTo("IN_PROGRESS");
        assertThat(payload.getPreviousStatus()).isEqualTo("CREATED");
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
        ArgumentCaptor<TripStatusChangedPayload> payloadCaptor =
                ArgumentCaptor.forClass(TripStatusChangedPayload.class);
        verify(webSocketEventService)
                .broadcastToTrip(
                        eq(tripId),
                        eq(WebSocketEventType.TRIP_STATUS_CHANGED),
                        payloadCaptor.capture());

        TripStatusChangedPayload payload = payloadCaptor.getValue();
        assertThat(payload.getNewStatus()).isEqualTo("CREATED");
        assertThat(payload.getPreviousStatus()).isNull();
    }
}
