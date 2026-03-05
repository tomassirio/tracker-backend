package com.tomassirio.wanderer.query.service.impl;

import com.tomassirio.wanderer.commons.domain.FriendRequestStatus;
import com.tomassirio.wanderer.commons.dto.FriendRequestResponse;
import com.tomassirio.wanderer.query.repository.FriendRequestRepository;
import com.tomassirio.wanderer.query.service.FriendRequestQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FriendRequestQueryServiceImpl implements FriendRequestQueryService {

    private final FriendRequestRepository friendRequestRepository;

    @Override
    public List<FriendRequestResponse> getPendingReceivedRequests(UUID userId) {
        log.info("Getting pending received friend requests for user {}", userId);
        return friendRequestRepository
                .findByReceiverIdAndStatus(userId, FriendRequestStatus.PENDING)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<FriendRequestResponse> getPendingSentRequests(UUID userId) {
        log.info("Getting pending sent friend requests for user {}", userId);
        return friendRequestRepository
                .findBySenderIdAndStatus(userId, FriendRequestStatus.PENDING)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private FriendRequestResponse mapToResponse(
            com.tomassirio.wanderer.commons.domain.FriendRequest request) {
        return new FriendRequestResponse(
                request.getId(),
                request.getSenderId(),
                request.getReceiverId(),
                request.getStatus(),
                request.getCreatedAt(),
                request.getUpdatedAt());
    }
}
