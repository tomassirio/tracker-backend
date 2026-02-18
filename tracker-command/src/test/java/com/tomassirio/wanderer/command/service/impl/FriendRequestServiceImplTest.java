package com.tomassirio.wanderer.command.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tomassirio.wanderer.command.event.FriendRequestAcceptedEvent;
import com.tomassirio.wanderer.command.event.FriendRequestCancelledEvent;
import com.tomassirio.wanderer.command.event.FriendRequestDeclinedEvent;
import com.tomassirio.wanderer.command.event.FriendRequestSentEvent;
import com.tomassirio.wanderer.command.event.FriendshipCreatedEvent;
import com.tomassirio.wanderer.command.repository.FriendRequestRepository;
import com.tomassirio.wanderer.command.service.FriendshipService;
import com.tomassirio.wanderer.commons.domain.FriendRequest;
import com.tomassirio.wanderer.commons.domain.FriendRequestStatus;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class FriendRequestServiceImplTest {

    @Mock private FriendRequestRepository friendRequestRepository;

    @Mock private FriendshipService friendshipService;

    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private FriendRequestServiceImpl friendRequestService;

    private UUID senderId;
    private UUID receiverId;
    private UUID requestId;
    private FriendRequest friendRequest;

    @BeforeEach
    void setUp() {
        senderId = UUID.randomUUID();
        receiverId = UUID.randomUUID();
        requestId = UUID.randomUUID();

        friendRequest =
                FriendRequest.builder()
                        .id(requestId)
                        .senderId(senderId)
                        .receiverId(receiverId)
                        .status(FriendRequestStatus.PENDING)
                        .createdAt(Instant.now())
                        .build();
    }

    @Test
    void sendFriendRequest_Success() {
        // Given
        when(friendshipService.areFriends(senderId, receiverId)).thenReturn(false);
        when(friendRequestRepository.findBySenderIdAndReceiverIdAndStatus(
                        senderId, receiverId, FriendRequestStatus.PENDING))
                .thenReturn(Optional.empty());

        // When
        UUID response = friendRequestService.sendFriendRequest(senderId, receiverId);

        // Then
        assertThat(response).isNotNull();

        ArgumentCaptor<FriendRequestSentEvent> eventCaptor =
                ArgumentCaptor.forClass(FriendRequestSentEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        FriendRequestSentEvent event = eventCaptor.getValue();
        assertThat(event.getRequestId()).isEqualTo(response);
        assertThat(event.getSenderId()).isEqualTo(senderId);
        assertThat(event.getReceiverId()).isEqualTo(receiverId);
        assertThat(event.getStatus()).isEqualTo(FriendRequestStatus.PENDING.name());
    }

    @Test
    void sendFriendRequest_ToSelf_ThrowsException() {
        // Given
        UUID userId = UUID.randomUUID();

        // When & Then
        assertThatThrownBy(() -> friendRequestService.sendFriendRequest(userId, userId))
                .isInstanceOf(IllegalArgumentException.class);

        verify(eventPublisher, never()).publishEvent(any(FriendRequestSentEvent.class));
    }

    @Test
    void sendFriendRequest_AlreadyFriends_ThrowsException() {
        // Given
        when(friendshipService.areFriends(senderId, receiverId)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> friendRequestService.sendFriendRequest(senderId, receiverId))
                .isInstanceOf(IllegalArgumentException.class);

        verify(eventPublisher, never()).publishEvent(any(FriendRequestSentEvent.class));
    }

    @Test
    void sendFriendRequest_AlreadyPending_ThrowsException() {
        // Given
        when(friendshipService.areFriends(senderId, receiverId)).thenReturn(false);
        when(friendRequestRepository.findBySenderIdAndReceiverIdAndStatus(
                        senderId, receiverId, FriendRequestStatus.PENDING))
                .thenReturn(Optional.of(friendRequest));

        // When & Then
        assertThatThrownBy(() -> friendRequestService.sendFriendRequest(senderId, receiverId))
                .isInstanceOf(IllegalArgumentException.class);

        verify(eventPublisher, never()).publishEvent(any(FriendRequestSentEvent.class));
    }

    @Test
    void acceptFriendRequest_Success() {
        // Given
        when(friendRequestRepository.findById(requestId)).thenReturn(Optional.of(friendRequest));

        // When
        UUID response = friendRequestService.acceptFriendRequest(requestId, receiverId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response).isEqualTo(requestId);

        // Verify FriendRequestAcceptedEvent was published
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher, times(2)).publishEvent(eventCaptor.capture());

        // Check that both events were published
        assertThat(eventCaptor.getAllValues())
                .hasSize(2)
                .anySatisfy(
                        event -> {
                            assertThat(event).isInstanceOf(FriendRequestAcceptedEvent.class);
                            FriendRequestAcceptedEvent acceptedEvent =
                                    (FriendRequestAcceptedEvent) event;
                            assertThat(acceptedEvent.getRequestId()).isEqualTo(requestId);
                        })
                .anySatisfy(
                        event -> {
                            assertThat(event).isInstanceOf(FriendshipCreatedEvent.class);
                            FriendshipCreatedEvent friendshipEvent = (FriendshipCreatedEvent) event;
                            assertThat(friendshipEvent.getUserId()).isEqualTo(senderId);
                            assertThat(friendshipEvent.getFriendId()).isEqualTo(receiverId);
                        });
    }

    @Test
    void acceptFriendRequest_NotReceiver_ThrowsException() {
        // Given
        UUID wrongUser = UUID.randomUUID();
        when(friendRequestRepository.findById(requestId)).thenReturn(Optional.of(friendRequest));

        // When & Then
        assertThatThrownBy(() -> friendRequestService.acceptFriendRequest(requestId, wrongUser))
                .isInstanceOf(IllegalArgumentException.class);

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void acceptFriendRequest_NotPending_ThrowsException() {
        // Given
        friendRequest.setStatus(FriendRequestStatus.ACCEPTED);
        when(friendRequestRepository.findById(requestId)).thenReturn(Optional.of(friendRequest));

        // When & Then
        assertThatThrownBy(() -> friendRequestService.acceptFriendRequest(requestId, receiverId))
                .isInstanceOf(IllegalArgumentException.class);

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void acceptFriendRequest_NotFound_ThrowsException() {
        // Given
        when(friendRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> friendRequestService.acceptFriendRequest(requestId, receiverId))
                .isInstanceOf(EntityNotFoundException.class);

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void deleteFriendRequest_AsSender_PublishesCancelledEvent() {
        // Given
        when(friendRequestRepository.findById(requestId)).thenReturn(Optional.of(friendRequest));

        // When
        UUID response = friendRequestService.deleteFriendRequest(requestId, senderId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response).isEqualTo(requestId);

        ArgumentCaptor<FriendRequestCancelledEvent> eventCaptor =
                ArgumentCaptor.forClass(FriendRequestCancelledEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        FriendRequestCancelledEvent event = eventCaptor.getValue();
        assertThat(event.getRequestId()).isEqualTo(requestId);
        assertThat(event.getSenderId()).isEqualTo(senderId);
        assertThat(event.getReceiverId()).isEqualTo(receiverId);
    }

    @Test
    void deleteFriendRequest_AsReceiver_PublishesDeclinedEvent() {
        // Given
        when(friendRequestRepository.findById(requestId)).thenReturn(Optional.of(friendRequest));

        // When
        UUID response = friendRequestService.deleteFriendRequest(requestId, receiverId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response).isEqualTo(requestId);

        ArgumentCaptor<FriendRequestDeclinedEvent> eventCaptor =
                ArgumentCaptor.forClass(FriendRequestDeclinedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        FriendRequestDeclinedEvent event = eventCaptor.getValue();
        assertThat(event.getRequestId()).isEqualTo(requestId);
        assertThat(event.getSenderId()).isEqualTo(senderId);
        assertThat(event.getReceiverId()).isEqualTo(receiverId);
    }

    @Test
    void deleteFriendRequest_NotSenderOrReceiver_ThrowsException() {
        // Given
        UUID wrongUser = UUID.randomUUID();
        when(friendRequestRepository.findById(requestId)).thenReturn(Optional.of(friendRequest));

        // When & Then
        assertThatThrownBy(() -> friendRequestService.deleteFriendRequest(requestId, wrongUser))
                .isInstanceOf(IllegalArgumentException.class);

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void deleteFriendRequest_NotPending_ThrowsException() {
        // Given
        friendRequest.setStatus(FriendRequestStatus.ACCEPTED);
        when(friendRequestRepository.findById(requestId)).thenReturn(Optional.of(friendRequest));

        // When & Then
        assertThatThrownBy(() -> friendRequestService.deleteFriendRequest(requestId, senderId))
                .isInstanceOf(IllegalArgumentException.class);

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void deleteFriendRequest_NotFound_ThrowsException() {
        // Given
        when(friendRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> friendRequestService.deleteFriendRequest(requestId, senderId))
                .isInstanceOf(EntityNotFoundException.class);

        verify(eventPublisher, never()).publishEvent(any());
    }
}
