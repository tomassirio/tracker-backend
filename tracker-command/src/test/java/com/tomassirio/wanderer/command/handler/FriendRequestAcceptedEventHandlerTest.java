package com.tomassirio.wanderer.command.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tomassirio.wanderer.command.event.FriendRequestAcceptedEvent;
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

        // Validation is done in service layer, handler uses getReferenceById
        when(friendRequestRepository.getReferenceById(requestId)).thenReturn(request);

        // When
        handler.handle(event);

        // Then
        ArgumentCaptor<FriendRequest> captor = ArgumentCaptor.forClass(FriendRequest.class);
        verify(friendRequestRepository).save(captor.capture());

        FriendRequest saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(FriendRequestStatus.ACCEPTED);
        assertThat(saved.getUpdatedAt()).isNotNull();
    }
}
