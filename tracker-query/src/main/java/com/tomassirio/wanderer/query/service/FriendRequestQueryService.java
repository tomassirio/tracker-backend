package com.tomassirio.wanderer.query.service;

import com.tomassirio.wanderer.commons.dto.FriendRequestResponse;
import java.util.List;
import java.util.UUID;

/**
 * Service responsible for querying friend request information.
 *
 * @author tomassirio
 * @since 0.3.7
 */
public interface FriendRequestQueryService {

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
