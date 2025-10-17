package com.tomassirio.wanderer.query.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.tomassirio.wanderer.commons.domain.FriendRequest;
import com.tomassirio.wanderer.commons.domain.FriendRequestStatus;
import com.tomassirio.wanderer.commons.dto.FriendRequestResponse;
import com.tomassirio.wanderer.query.repository.FriendRequestRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FriendRequestQueryServiceImplTest {

    @Mock private FriendRequestRepository friendRequestRepository;

    @InjectMocks private FriendRequestQueryServiceImpl friendRequestQueryService;

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
    void getPendingReceivedRequests_Success() {
        when(friendRequestRepository.findByReceiverIdAndStatus(
                        receiverId, FriendRequestStatus.PENDING))
                .thenReturn(List.of(friendRequest));

        List<FriendRequestResponse> responses =
                friendRequestQueryService.getPendingReceivedRequests(receiverId);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(requestId, responses.get(0).id());
        assertEquals(senderId, responses.get(0).senderId());
        assertEquals(receiverId, responses.get(0).receiverId());
    }

    @Test
    void getPendingReceivedRequests_EmptyList() {
        when(friendRequestRepository.findByReceiverIdAndStatus(
                        receiverId, FriendRequestStatus.PENDING))
                .thenReturn(List.of());

        List<FriendRequestResponse> responses =
                friendRequestQueryService.getPendingReceivedRequests(receiverId);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    void getPendingSentRequests_Success() {
        when(friendRequestRepository.findBySenderIdAndStatus(senderId, FriendRequestStatus.PENDING))
                .thenReturn(List.of(friendRequest));

        List<FriendRequestResponse> responses =
                friendRequestQueryService.getPendingSentRequests(senderId);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(requestId, responses.get(0).id());
        assertEquals(senderId, responses.get(0).senderId());
        assertEquals(receiverId, responses.get(0).receiverId());
    }

    @Test
    void getPendingSentRequests_EmptyList() {
        when(friendRequestRepository.findBySenderIdAndStatus(senderId, FriendRequestStatus.PENDING))
                .thenReturn(List.of());

        List<FriendRequestResponse> responses =
                friendRequestQueryService.getPendingSentRequests(senderId);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }
}
