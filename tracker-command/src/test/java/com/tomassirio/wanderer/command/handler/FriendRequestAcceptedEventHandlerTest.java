package com.tomassirio.wanderer.command.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.tomassirio.wanderer.command.event.FriendRequestAcceptedEvent;
import com.tomassirio.wanderer.command.repository.FriendRequestRepository;
import com.tomassirio.wanderer.commons.domain.FriendRequest;
import com.tomassirio.wanderer.commons.domain.FriendRequestStatus;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FriendRequestAcceptedEventHandlerTest {

    @Mock private FriendRequestRepository friendRequestRepository;

    @InjectMocks private FriendRequestAcceptedEventHandler handler;

    @Test
    void handle_shouldUpdateFriendRequestStatusToAccepted() {
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

        FriendRequestAcceptedEvent event =
                FriendRequestAcceptedEvent.builder()
                        .requestId(requestId)
                        .senderId(request.getSenderId())
                        .receiverId(request.getReceiverId())
                        .build();

        when(friendRequestRepository.findById(requestId)).thenReturn(Optional.of(request));

        // When
        handler.handle(event);

        // Then - entity is managed, no need to verify save
        assertThat(request.getStatus()).isEqualTo(FriendRequestStatus.ACCEPTED);
        assertThat(request.getUpdatedAt()).isNotNull();
    }
}
