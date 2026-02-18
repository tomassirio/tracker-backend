package com.tomassirio.wanderer.command.service.impl;

import com.tomassirio.wanderer.command.event.DomainEvent;
import com.tomassirio.wanderer.command.event.FriendRequestAcceptedEvent;
import com.tomassirio.wanderer.command.event.FriendRequestCancelledEvent;
import com.tomassirio.wanderer.command.event.FriendRequestDeclinedEvent;
import com.tomassirio.wanderer.command.event.FriendRequestSentEvent;
import com.tomassirio.wanderer.command.event.FriendshipCreatedEvent;
import com.tomassirio.wanderer.command.repository.FriendRequestRepository;
import com.tomassirio.wanderer.command.service.FriendRequestService;
import com.tomassirio.wanderer.command.service.FriendshipService;
import com.tomassirio.wanderer.commons.domain.FriendRequest;
import com.tomassirio.wanderer.commons.domain.FriendRequestStatus;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FriendRequestServiceImpl implements FriendRequestService {

    private final FriendRequestRepository friendRequestRepository;
    private final FriendshipService friendshipService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public UUID sendFriendRequest(UUID senderId, UUID receiverId) {
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

        // Pre-generate ID and timestamp
        UUID requestId = UUID.randomUUID();
        Instant createdAt = Instant.now();

        // Publish event - persistence handler will write to DB
        eventPublisher.publishEvent(
                FriendRequestSentEvent.builder()
                        .requestId(requestId)
                        .senderId(senderId)
                        .receiverId(receiverId)
                        .status(FriendRequestStatus.PENDING.name())
                        .createdAt(createdAt)
                        .build());

        log.info("Friend request created with ID: {}", requestId);

        return requestId;
    }

    @Override
    public UUID acceptFriendRequest(UUID requestId, UUID userId) {
        log.info("User {} accepting friend request {}", userId, requestId);

        FriendRequest request =
                friendRequestRepository
                        .findById(requestId)
                        .orElseThrow(() -> new EntityNotFoundException("Friend request not found"));

        if (!request.getReceiverId().equals(userId)) {
            throw new IllegalArgumentException("Only the receiver can accept the friend request");
        }

        if (request.getStatus() != FriendRequestStatus.PENDING) {
            throw new IllegalArgumentException("Friend request is not pending");
        }

        // Publish event - persistence handler will update DB
        eventPublisher.publishEvent(
                FriendRequestAcceptedEvent.builder()
                        .requestId(requestId)
                        .senderId(request.getSenderId())
                        .receiverId(request.getReceiverId())
                        .build());

        // Create bidirectional friendship via event
        eventPublisher.publishEvent(
                FriendshipCreatedEvent.builder()
                        .userId(request.getSenderId())
                        .friendId(request.getReceiverId())
                        .build());

        log.info("Friend request {} accepted", requestId);

        return requestId;
    }

    @Override
    public UUID deleteFriendRequest(UUID requestId, UUID userId) {
        log.info("User {} deleting friend request {}", userId, requestId);

        FriendRequest request =
                friendRequestRepository
                        .findById(requestId)
                        .orElseThrow(() -> new EntityNotFoundException("Friend request not found"));

        if (request.getStatus() != FriendRequestStatus.PENDING) {
            throw new IllegalArgumentException("Only pending friend requests can be deleted");
        }

        boolean isSender = request.getSenderId().equals(userId);
        boolean isReceiver = request.getReceiverId().equals(userId);

        DomainEvent event =
                isSender
                        ? FriendRequestCancelledEvent.builder()
                                .requestId(requestId)
                                .senderId(request.getSenderId())
                                .receiverId(request.getReceiverId())
                                .build()
                        : isReceiver
                                ? FriendRequestDeclinedEvent.builder()
                                        .requestId(requestId)
                                        .senderId(request.getSenderId())
                                        .receiverId(request.getReceiverId())
                                        .build()
                                : null;

        if (event == null) {
            throw new IllegalArgumentException(
                    "Only the sender or receiver can delete the friend request");
        }

        eventPublisher.publishEvent(event);
        log.info(
                "Friend request {} {} by {}",
                requestId,
                isSender ? "cancelled" : "declined",
                isSender ? "sender" : "receiver");

        return requestId;
    }
}
