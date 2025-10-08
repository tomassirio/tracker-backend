package com.tomassirio.wanderer.query.service;

import com.tomassirio.wanderer.query.dto.UserResponse;
import java.util.UUID;

/**
 * Service interface for user query operations.
 * Provides methods to retrieve user information.
 *
 * @since 0.1.8
 */
public interface UserQueryService {

    /**
     * Retrieves a user by their unique identifier.
     *
     * @param id the user's unique identifier
     * @return the user response containing user details
     * @throws jakarta.persistence.EntityNotFoundException if the user is not found
     */
    UserResponse getUserById(UUID id);

    /**
     * Retrieves a user by their username.
     *
     * @param username the user's username
     * @return the user response containing user details
     * @throws jakarta.persistence.EntityNotFoundException if the user is not found
     */
    UserResponse getUserByUsername(String username);

    /**
     * Retrieves a user by their email address.
     *
     * @param email the user's email address
     * @return the user response containing user details
     * @throws jakarta.persistence.EntityNotFoundException if the user is not found
     */
    UserResponse getUserByEmail(String email);
}
