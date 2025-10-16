package com.tomassirio.wanderer.command.service;

import java.util.UUID;

/**
 * Service responsible for handling friendship operations.
 *
 * @author tomassirio
 * @since 0.3.7
 */
public interface FriendshipService {

    /**
     * Check if two users are friends.
     *
     * @param userId first user ID
     * @param friendId second user ID
     * @return true if users are friends, false otherwise
     */
    boolean areFriends(UUID userId, UUID friendId);

    /**
     * Create a friendship between two users (bidirectional).
     *
     * @param userId first user ID
     * @param friendId second user ID
     */
    void createFriendship(UUID userId, UUID friendId);

    /**
     * Remove a friendship between two users (bidirectional).
     *
     * @param userId first user ID
     * @param friendId second user ID
     */
    void removeFriendship(UUID userId, UUID friendId);
}
