package com.tomassirio.wanderer.query.service;

import com.tomassirio.wanderer.commons.dto.UserFollowResponse;
import java.util.List;
import java.util.UUID;

/**
 * Service responsible for querying user follow information.
 *
 * @author tomassirio
 * @since 0.4.0
 */
public interface UserFollowQueryService {

    /**
     * Get all users that the current user follows.
     *
     * @param userId the ID of the user
     * @return list of follows
     */
    List<UserFollowResponse> getFollowing(UUID userId);

    /**
     * Get all users that follow the current user.
     *
     * @param userId the ID of the user
     * @return list of followers
     */
    List<UserFollowResponse> getFollowers(UUID userId);
}
