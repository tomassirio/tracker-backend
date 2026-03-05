package com.tomassirio.wanderer.command.service;

import java.util.UUID;

/**
 * Service interface for admin operations in the command service. Delegates role management to the
 * auth service via Feign.
 *
 * @since 0.5.3
 */
public interface AdminService {

    /**
     * Promotes a user to admin role by delegating to the auth service.
     *
     * @param userId the ID of the user to promote
     */
    void promoteToAdmin(UUID userId);

    /**
     * Demotes a user from admin role by delegating to the auth service.
     *
     * @param userId the ID of the user to demote
     */
    void demoteFromAdmin(UUID userId);
}
