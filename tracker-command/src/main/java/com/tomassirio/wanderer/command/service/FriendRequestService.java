package com.tomassirio.wanderer.command.service;

import com.tomassirio.wanderer.command.dto.FriendRequestResponse;
import java.util.List;
import java.util.UUID;

/**
 * Service responsible for handling friend request operations.
 *
 * @author tomassirio
 * @since 0.3.7
 */
public interface FriendRequestService {

    /**
     * Send a friend request from current user to another user.
     *
     * @param senderId the ID of the user sending the request
     * @param receiverId the ID of the user receiving the request
     * @return the created friend request
     * @throws IllegalArgumentException if a pending request already exists or users are already
     *     friends
     */
    FriendRequestResponse sendFriendRequest(UUID senderId, UUID receiverId);

    /**
     * Accept a friend request.
     *
     * @param requestId the ID of the friend request
     * @param userId the ID of the user accepting the request
     * @return the updated friend request
     * @throws jakarta.persistence.EntityNotFoundException if request not found
     * @throws IllegalArgumentException if user is not the receiver or request is not pending
     */
    FriendRequestResponse acceptFriendRequest(UUID requestId, UUID userId);

    /**
     * Decline a friend request.
     *
     * @param requestId the ID of the friend request
     * @param userId the ID of the user declining the request
     * @return the updated friend request
     * @throws jakarta.persistence.EntityNotFoundException if request not found
     * @throws IllegalArgumentException if user is not the receiver or request is not pending
     */
    FriendRequestResponse declineFriendRequest(UUID requestId, UUID userId);

    /**
     * Get all pending friend requests received by a user.
     *
     * @param userId the ID of the user
     * @return list of pending friend requests
     */
    List<FriendRequestResponse> getPendingReceivedRequests(UUID userId);

    /**
     * Get all pending friend requests sent by a user.
     *
     * @param userId the ID of the user
     * @return list of pending friend requests
     */
    List<FriendRequestResponse> getPendingSentRequests(UUID userId);
}
