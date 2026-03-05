package com.tomassirio.wanderer.auth.service;

import java.util.UUID;

/**
 * Service interface for admin operations. Provides methods for credential management.
 *
 * @since 0.5.2
 */
public interface AdminService {

    /**
     * Deletes the credentials for a user. Called by the command service after user data has been
     * cleaned up.
     *
     * @param userId the ID of the user whose credentials should be deleted
     * @throws IllegalArgumentException if user not found
     * @throws IllegalStateException if trying to delete the last admin user
     */
    void deleteCredentials(UUID userId);
}
