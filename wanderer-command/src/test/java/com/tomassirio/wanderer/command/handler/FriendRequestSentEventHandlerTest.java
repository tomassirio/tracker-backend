package com.tomassirio.wanderer.command.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.tomassirio.wanderer.command.event.FriendRequestSentEvent;
import com.tomassirio.wanderer.command.repository.FriendRequestRepository;
import com.tomassirio.wanderer.commons.domain.FriendRequest;
import com.tomassirio.wanderer.commons.domain.FriendRequestStatus;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FriendRequestSentEventHandlerTest {

    @Mock private FriendRequestRepository friendRequestRepository;

    @InjectMocks private FriendRequestSentEventHandler handler;

    @Test
    void handle_shouldPersistFriendRequest() {
        // Given
        UUID requestId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        Instant createdAt = Instant.now();

        FriendRequestSentEvent event =
                FriendRequestSentEvent.builder()
                        .requestId(requestId)
                        .senderId(senderId)
                        .receiverId(receiverId)
                        .status(FriendRequestStatus.PENDING.name())
                        .createdAt(createdAt)
                        .build();

        // When
        handler.handle(event);

        // Then
        ArgumentCaptor<FriendRequest> captor = ArgumentCaptor.forClass(FriendRequest.class);
        verify(friendRequestRepository).save(captor.capture());

        FriendRequest saved = captor.getValue();
        assertThat(saved.getId()).isEqualTo(requestId);
        assertThat(saved.getSenderId()).isEqualTo(senderId);
        assertThat(saved.getReceiverId()).isEqualTo(receiverId);
        assertThat(saved.getStatus()).isEqualTo(FriendRequestStatus.PENDING);
        assertThat(saved.getCreatedAt()).isEqualTo(createdAt);
    }
}
