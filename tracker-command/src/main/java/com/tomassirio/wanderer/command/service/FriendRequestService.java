package com.tomassirio.wanderer.command.service;

import java.util.UUID;

/**
 * Service responsible for handling friend request command operations.
 *
 * @author tomassirio
 * @since 0.4.0
 */
public interface FriendRequestService {

    /**
     * Send a friend request from current user to another user.
     *
     * @param senderId the ID of the user sending the request
     * @param receiverId the ID of the user receiving the request
     * @return the UUID of the created friend request
     * @throws IllegalArgumentException if a pending request already exists or users are already
     *     friends
     */
    UUID sendFriendRequest(UUID senderId, UUID receiverId);

    /**
     * Accept a friend request.
     *
     * @param requestId the ID of the friend request
     * @param userId the ID of the user accepting the request
     * @return the UUID of the friend request
     * @throws jakarta.persistence.EntityNotFoundException if request not found
     * @throws IllegalArgumentException if user is not the receiver or request is not pending
     */
    UUID acceptFriendRequest(UUID requestId, UUID userId);

    /**
     * Delete a friend request. If the user is the sender, it cancels the request. If the user is
     * the receiver, it declines the request.
     *
     * @param requestId the ID of the friend request
     * @param userId the ID of the user deleting the request (must be sender or receiver)
     * @return the UUID of the friend request
     * @throws jakarta.persistence.EntityNotFoundException if request not found
     * @throws IllegalArgumentException if user is not sender/receiver or request is not pending
     */
    UUID deleteFriendRequest(UUID requestId, UUID userId);
}
