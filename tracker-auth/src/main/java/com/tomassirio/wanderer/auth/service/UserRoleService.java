package com.tomassirio.wanderer.auth.service;

import com.tomassirio.wanderer.commons.security.Role;
import java.util.Set;
import java.util.UUID;

/**
 * Service interface for user role management operations. Provides methods for promoting and
 * demoting users between roles.
 *
 * @since 0.5.2
 */
public interface UserRoleService {

    /**
     * Promotes a user to admin role. Adds ADMIN role while keeping existing roles.
     *
     * @param userId the ID of the user to promote
     * @throws IllegalArgumentException if user not found or already has admin role
     */
    void promoteToAdmin(UUID userId);

    /**
     * Demotes a user from admin role. Removes ADMIN role but keeps USER role.
     *
     * @param userId the ID of the user to demote
     * @throws IllegalArgumentException if user not found or doesn't have admin role
     */
    void demoteFromAdmin(UUID userId);

    /**
     * Gets the roles for a specific user.
     *
     * @param userId the ID of the user
     * @return the set of roles assigned to the user
     * @throws IllegalArgumentException if user not found
     */
    Set<Role> getUserRoles(UUID userId);

    /**
     * Checks if there are any admin users in the system.
     *
     * @return true if at least one admin exists, false otherwise
     */
    boolean hasAnyAdmins();
}
