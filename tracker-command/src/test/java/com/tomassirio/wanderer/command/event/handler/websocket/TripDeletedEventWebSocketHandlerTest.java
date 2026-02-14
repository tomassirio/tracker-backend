package com.tomassirio.wanderer.command.event.handler.websocket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.tomassirio.wanderer.command.event.TripDeletedEvent;
import com.tomassirio.wanderer.command.websocket.TripLifecyclePayload;
import com.tomassirio.wanderer.command.websocket.WebSocketEventService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TripDeletedEventWebSocketHandlerTest {

    @Mock private WebSocketEventService webSocketEventService;

    @InjectMocks private TripDeletedEventWebSocketHandler handler;

    @Test
    void handle_whenEventReceived_shouldBroadcastTripDeleted() {
        // Given
        UUID tripId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        TripDeletedEvent event = TripDeletedEvent.builder().tripId(tripId).ownerId(ownerId).build();

        // When
        handler.handle(event);

        // Then
        ArgumentCaptor<TripLifecyclePayload> payloadCaptor =
                ArgumentCaptor.forClass(TripLifecyclePayload.class);
        verify(webSocketEventService).broadcastTripDeleted(payloadCaptor.capture());

        TripLifecyclePayload payload = payloadCaptor.getValue();
        assertThat(payload.getTripId()).isEqualTo(tripId);
        assertThat(payload.getOwnerId()).isEqualTo(ownerId);
        assertThat(payload.getTripName()).isNull();
        assertThat(payload.getVisibility()).isNull();
    }
}
