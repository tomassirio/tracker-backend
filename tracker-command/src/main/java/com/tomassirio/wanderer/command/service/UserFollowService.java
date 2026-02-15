package com.tomassirio.wanderer.command.service;

import java.util.UUID;

/**
 * Service responsible for handling user follow operations.
 *
 * @author tomassirio
 * @since 0.4.0
 */
public interface UserFollowService {

    /**
     * Follow a user.
     *
     * @param followerId the ID of the user following
     * @param followedId the ID of the user being followed
     * @return the UUID of the created follow relationship
     * @throws IllegalArgumentException if already following
     */
    UUID followUser(UUID followerId, UUID followedId);

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
