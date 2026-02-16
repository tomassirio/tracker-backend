package com.tomassirio.wanderer.command.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.tomassirio.wanderer.command.event.FriendRequestDeclinedEvent;
import com.tomassirio.wanderer.commons.domain.FriendRequest;
import com.tomassirio.wanderer.commons.domain.FriendRequestStatus;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FriendRequestDeclinedEventHandlerTest {

    @Mock private EntityManager entityManager;

    @InjectMocks private FriendRequestDeclinedEventHandler handler;

    @Test
    void handle_shouldUpdateFriendRequestStatusToDeclined() {
        // Given
        UUID requestId = UUID.randomUUID();
        FriendRequest request =
                FriendRequest.builder()
                        .id(requestId)
                        .senderId(UUID.randomUUID())
                        .receiverId(UUID.randomUUID())
                        .status(FriendRequestStatus.PENDING)
                        .createdAt(Instant.now())
                        .build();

        FriendRequestDeclinedEvent event =
                FriendRequestDeclinedEvent.builder()
                        .requestId(requestId)
                        .senderId(request.getSenderId())
                        .receiverId(request.getReceiverId())
                        .build();

        when(entityManager.find(FriendRequest.class, requestId)).thenReturn(request);

        // When
        handler.handle(event);

        // Then - entity is managed, no need to verify save
        assertThat(request.getStatus()).isEqualTo(FriendRequestStatus.DECLINED);
        assertThat(request.getUpdatedAt()).isNotNull();
    }
}
