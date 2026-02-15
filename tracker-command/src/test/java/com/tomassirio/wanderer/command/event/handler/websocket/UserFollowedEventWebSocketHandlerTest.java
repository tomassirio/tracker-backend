package com.tomassirio.wanderer.command.event.handler.websocket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.tomassirio.wanderer.command.event.UserFollowedEvent;
import com.tomassirio.wanderer.command.websocket.UserFollowPayload;
import com.tomassirio.wanderer.command.websocket.WebSocketEventService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserFollowedEventWebSocketHandlerTest {

    @Mock private WebSocketEventService webSocketEventService;

    @InjectMocks private UserFollowedEventWebSocketHandler handler;

    @Test
    void handle_whenEventReceived_shouldBroadcastUserFollowed() {
        // Given
        UUID followId = UUID.randomUUID();
        UUID followerId = UUID.randomUUID();
        UUID followedId = UUID.randomUUID();
        UserFollowedEvent event =
                UserFollowedEvent.builder()
                        .followId(followId)
                        .followerId(followerId)
                        .followedId(followedId)
                        .build();

        // When
        handler.handle(event);

        // Then
        ArgumentCaptor<UserFollowPayload> payloadCaptor =
                ArgumentCaptor.forClass(UserFollowPayload.class);
        verify(webSocketEventService).broadcastUserFollowed(payloadCaptor.capture());

        UserFollowPayload payload = payloadCaptor.getValue();
        assertThat(payload.getFollowId()).isEqualTo(followId);
        assertThat(payload.getFollowerId()).isEqualTo(followerId);
        assertThat(payload.getFollowedId()).isEqualTo(followedId);
    }
}
