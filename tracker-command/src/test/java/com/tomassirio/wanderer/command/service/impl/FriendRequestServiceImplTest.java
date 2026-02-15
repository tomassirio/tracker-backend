package com.tomassirio.wanderer.command.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
        when(friendshipService.areFriends(senderId, receiverId)).thenReturn(false);
        when(friendRequestRepository.findBySenderIdAndReceiverIdAndStatus(
                        senderId, receiverId, FriendRequestStatus.PENDING))
                .thenReturn(Optional.empty());
        when(friendRequestRepository.save(any(FriendRequest.class))).thenReturn(friendRequest);

        UUID response = friendRequestService.sendFriendRequest(senderId, receiverId);

        assertNotNull(response);
        assertEquals(requestId, response);

        verify(friendRequestRepository).save(any(FriendRequest.class));
    }

    @Test
    void sendFriendRequest_ToSelf_ThrowsException() {
        UUID userId = UUID.randomUUID();

        assertThrows(
                IllegalArgumentException.class,
                () -> friendRequestService.sendFriendRequest(userId, userId));

        verify(friendRequestRepository, never()).save(any());
    }

    @Test
    void sendFriendRequest_AlreadyFriends_ThrowsException() {
        when(friendshipService.areFriends(senderId, receiverId)).thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> friendRequestService.sendFriendRequest(senderId, receiverId));

        verify(friendRequestRepository, never()).save(any());
    }

    @Test
    void sendFriendRequest_AlreadyPending_ThrowsException() {
        when(friendshipService.areFriends(senderId, receiverId)).thenReturn(false);
        when(friendRequestRepository.findBySenderIdAndReceiverIdAndStatus(
                        senderId, receiverId, FriendRequestStatus.PENDING))
                .thenReturn(Optional.of(friendRequest));

        assertThrows(
                IllegalArgumentException.class,
                () -> friendRequestService.sendFriendRequest(senderId, receiverId));

        verify(friendRequestRepository, never()).save(any());
    }

    @Test
    void acceptFriendRequest_Success() {
        when(friendRequestRepository.findById(requestId)).thenReturn(Optional.of(friendRequest));
        when(friendRequestRepository.save(any(FriendRequest.class))).thenReturn(friendRequest);

        UUID response = friendRequestService.acceptFriendRequest(requestId, receiverId);

        assertNotNull(response);
        assertEquals(requestId, response);
        assertEquals(FriendRequestStatus.ACCEPTED, friendRequest.getStatus());
        assertNotNull(friendRequest.getUpdatedAt());

        verify(friendshipService).createFriendship(senderId, receiverId);
    }

    @Test
    void acceptFriendRequest_NotReceiver_ThrowsException() {
        UUID wrongUser = UUID.randomUUID();
        when(friendRequestRepository.findById(requestId)).thenReturn(Optional.of(friendRequest));

        assertThrows(
                IllegalArgumentException.class,
                () -> friendRequestService.acceptFriendRequest(requestId, wrongUser));

        verify(friendshipService, never()).createFriendship(any(), any());
    }

    @Test
    void acceptFriendRequest_NotPending_ThrowsException() {
        friendRequest.setStatus(FriendRequestStatus.ACCEPTED);
        when(friendRequestRepository.findById(requestId)).thenReturn(Optional.of(friendRequest));

        assertThrows(
                IllegalArgumentException.class,
                () -> friendRequestService.acceptFriendRequest(requestId, receiverId));

        verify(friendshipService, never()).createFriendship(any(), any());
    }

    @Test
    void acceptFriendRequest_NotFound_ThrowsException() {
        when(friendRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> friendRequestService.acceptFriendRequest(requestId, receiverId));

        verify(friendshipService, never()).createFriendship(any(), any());
    }

    @Test
    void declineFriendRequest_Success() {
        when(friendRequestRepository.findById(requestId)).thenReturn(Optional.of(friendRequest));
        when(friendRequestRepository.save(any(FriendRequest.class))).thenReturn(friendRequest);

        UUID response = friendRequestService.declineFriendRequest(requestId, receiverId);

        assertNotNull(response);
        assertEquals(requestId, response);
        assertEquals(FriendRequestStatus.DECLINED, friendRequest.getStatus());
        assertNotNull(friendRequest.getUpdatedAt());
    }

    @Test
    void declineFriendRequest_NotReceiver_ThrowsException() {
        UUID wrongUser = UUID.randomUUID();
        when(friendRequestRepository.findById(requestId)).thenReturn(Optional.of(friendRequest));

        assertThrows(
                IllegalArgumentException.class,
                () -> friendRequestService.declineFriendRequest(requestId, wrongUser));
    }

    @Test
    void declineFriendRequest_NotPending_ThrowsException() {
        friendRequest.setStatus(FriendRequestStatus.DECLINED);
        when(friendRequestRepository.findById(requestId)).thenReturn(Optional.of(friendRequest));

        assertThrows(
                IllegalArgumentException.class,
                () -> friendRequestService.declineFriendRequest(requestId, receiverId));
    }
}
