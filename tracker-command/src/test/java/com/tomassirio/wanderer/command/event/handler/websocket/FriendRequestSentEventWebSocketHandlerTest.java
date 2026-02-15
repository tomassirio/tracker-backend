package com.tomassirio.wanderer.command.event.handler.websocket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.tomassirio.wanderer.command.event.FriendRequestSentEvent;
import com.tomassirio.wanderer.command.websocket.FriendRequestPayload;
import com.tomassirio.wanderer.command.websocket.WebSocketEventService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FriendRequestSentEventWebSocketHandlerTest {

    @Mock private WebSocketEventService webSocketEventService;

    @InjectMocks private FriendRequestSentEventWebSocketHandler handler;

    @Test
    void handle_whenEventReceived_shouldBroadcastFriendRequestSent() {
        // Given
        UUID requestId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        FriendRequestSentEvent event =
                FriendRequestSentEvent.builder()
                        .requestId(requestId)
                        .senderId(senderId)
                        .receiverId(receiverId)
                        .status("PENDING")
                        .build();

        // When
        handler.handle(event);

        // Then
        ArgumentCaptor<FriendRequestPayload> payloadCaptor =
                ArgumentCaptor.forClass(FriendRequestPayload.class);
        verify(webSocketEventService).broadcastFriendRequestSent(payloadCaptor.capture());

        FriendRequestPayload payload = payloadCaptor.getValue();
        assertThat(payload.getRequestId()).isEqualTo(requestId);
        assertThat(payload.getSenderId()).isEqualTo(senderId);
        assertThat(payload.getReceiverId()).isEqualTo(receiverId);
        assertThat(payload.getStatus()).isEqualTo("PENDING");
    }
}
