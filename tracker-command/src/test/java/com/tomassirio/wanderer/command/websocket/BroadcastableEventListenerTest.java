package com.tomassirio.wanderer.command.websocket;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tomassirio.wanderer.command.event.Broadcastable;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BroadcastableEventListenerTest {

    @Mock private WebSocketEventService webSocketEventService;

    @Mock private Broadcastable broadcastableEvent;

    private BroadcastableEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new BroadcastableEventListener(webSocketEventService);
    }

    @Test
    void onBroadcastableEvent_shouldBroadcastEvent() {
        // Given
        UUID targetId = UUID.randomUUID();
        String eventType = "TEST_EVENT";
        String topic = "/topic/test/" + targetId;

        when(broadcastableEvent.getEventType()).thenReturn(eventType);
        when(broadcastableEvent.getTopic()).thenReturn(topic);

        // When
        listener.onBroadcastableEvent(broadcastableEvent);

        // Then
        verify(webSocketEventService).broadcast(broadcastableEvent);
    }
}
