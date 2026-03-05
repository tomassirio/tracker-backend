package com.tomassirio.wanderer.query.service;

import com.tomassirio.wanderer.commons.dto.UserFollowResponse;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for querying user follows.
 *
 * @author tomassirio
 * @since 0.4.5
 */
public interface UserFollowService {

    /**
     * Get list of users that the current user is following.
     *
     * @param followerId the ID of the user whose following list to retrieve
     * @return list of follows
     */
    List<UserFollowResponse> getFollowing(UUID followerId);

    /**
     * Get list of users that are following the current user.
     *
     * @param followedId the ID of the user whose followers to retrieve
     * @return list of followers
     */
    List<UserFollowResponse> getFollowers(UUID followedId);
}
