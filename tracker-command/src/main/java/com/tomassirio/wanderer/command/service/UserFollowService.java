package com.tomassirio.wanderer.command.service;

import com.tomassirio.wanderer.command.dto.UserFollowResponse;
import java.util.UUID;

/**
 * Service responsible for handling user follow operations.
 *
 * @author tomassirio
 * @since 0.3.7
 */
public interface UserFollowService {

    /**
     * Follow a user.
     *
     * @param followerId the ID of the user following
     * @param followedId the ID of the user being followed
     * @return the created follow relationship
     * @throws IllegalArgumentException if already following
     */
    UserFollowResponse followUser(UUID followerId, UUID followedId);

    /**
     * Unfollow a user.
     *
     * @param followerId the ID of the user unfollowing
     * @param followedId the ID of the user being unfollowed
     */
    void unfollowUser(UUID followerId, UUID followedId);

    /**
     * Check if a user is following another user.
     *
     * @param followerId the ID of the follower
     * @param followedId the ID of the followed user
     * @return true if following, false otherwise
     */
    boolean isFollowing(UUID followerId, UUID followedId);
}
