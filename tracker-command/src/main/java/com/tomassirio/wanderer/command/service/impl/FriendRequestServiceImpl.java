package com.tomassirio.wanderer.command.service.impl;

import com.tomassirio.wanderer.commons.dto.FriendRequestResponse;
import com.tomassirio.wanderer.command.repository.FriendRequestRepository;
import com.tomassirio.wanderer.command.service.FriendRequestService;
import com.tomassirio.wanderer.command.service.FriendshipService;
import com.tomassirio.wanderer.commons.domain.FriendRequest;
import com.tomassirio.wanderer.commons.domain.FriendRequestStatus;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FriendRequestServiceImpl implements FriendRequestService {

    private final FriendRequestRepository friendRequestRepository;
    private final FriendshipService friendshipService;

    @Override
    @Transactional
    public FriendRequestResponse sendFriendRequest(UUID senderId, UUID receiverId) {
        log.info("Sending friend request from {} to {}", senderId, receiverId);

        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException("Cannot send friend request to yourself");
        }

        if (friendshipService.areFriends(senderId, receiverId)) {
            throw new IllegalArgumentException("Users are already friends");
        }

        // Check for existing pending request
        if (friendRequestRepository
                .findBySenderIdAndReceiverIdAndStatus(
                        senderId, receiverId, FriendRequestStatus.PENDING)
                .isPresent()) {
            throw new IllegalArgumentException("Friend request already sent");
        }

        FriendRequest request =
                FriendRequest.builder()
                        .senderId(senderId)
                        .receiverId(receiverId)
                        .status(FriendRequestStatus.PENDING)
                        .createdAt(Instant.now())
                        .build();

        FriendRequest saved = friendRequestRepository.save(request);
        log.info("Friend request created with ID: {}", saved.getId());

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public FriendRequestResponse acceptFriendRequest(UUID requestId, UUID userId) {
        log.info("User {} accepting friend request {}", userId, requestId);

        FriendRequest request =
                friendRequestRepository
                        .findById(requestId)
                        .orElseThrow(
                                () -> new EntityNotFoundException("Friend request not found"));

        if (!request.getReceiverId().equals(userId)) {
            throw new IllegalArgumentException("Only the receiver can accept the friend request");
        }

        if (request.getStatus() != FriendRequestStatus.PENDING) {
            throw new IllegalArgumentException("Friend request is not pending");
        }

        request.setStatus(FriendRequestStatus.ACCEPTED);
        request.setUpdatedAt(Instant.now());

        FriendRequest updated = friendRequestRepository.save(request);

        // Create bidirectional friendship
        friendshipService.createFriendship(request.getSenderId(), request.getReceiverId());

        log.info("Friend request {} accepted", requestId);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public FriendRequestResponse declineFriendRequest(UUID requestId, UUID userId) {
        log.info("User {} declining friend request {}", userId, requestId);

        FriendRequest request =
                friendRequestRepository
                        .findById(requestId)
                        .orElseThrow(
                                () -> new EntityNotFoundException("Friend request not found"));

        if (!request.getReceiverId().equals(userId)) {
            throw new IllegalArgumentException("Only the receiver can decline the friend request");
        }

        if (request.getStatus() != FriendRequestStatus.PENDING) {
            throw new IllegalArgumentException("Friend request is not pending");
        }

        request.setStatus(FriendRequestStatus.DECLINED);
        request.setUpdatedAt(Instant.now());

        FriendRequest updated = friendRequestRepository.save(request);

        log.info("Friend request {} declined", requestId);
        return mapToResponse(updated);
    }

    private FriendRequestResponse mapToResponse(FriendRequest request) {
        return new FriendRequestResponse(
                request.getId(),
                request.getSenderId(),
                request.getReceiverId(),
                request.getStatus(),
                request.getCreatedAt(),
                request.getUpdatedAt());
    }
}
