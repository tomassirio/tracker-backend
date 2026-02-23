package com.tomassirio.wanderer.auth.service;

import java.util.UUID;

/**
 * Service interface for admin operations. Provides methods for admin-level user management.
 *
 * @since 0.5.2
 */
public interface AdminService {

    /**
     * Deletes a user from the system. This removes the user's credentials from the auth service and
     * the user record from the command service.
     *
     * @param userId the ID of the user to delete
     * @throws IllegalArgumentException if user not found
     * @throws IllegalStateException if trying to delete the last admin user
     */
    void deleteUser(UUID userId);
}
