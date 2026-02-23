package com.tomassirio.wanderer.query.service;

import com.tomassirio.wanderer.query.dto.UserAdminResponse;
import com.tomassirio.wanderer.query.dto.UserResponse;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for user query operations. Provides methods to retrieve user information.
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
     * Retrieves all users with pagination and sorting support.
     *
     * @param pageable pagination and sorting information
     * @return a page of user responses
     */
    Page<UserResponse> getAllUsers(Pageable pageable);

    /**
     * Retrieves all users with statistics for admin view. Includes friends count, followers count,
     * and trips count for each user.
     *
     * @param pageable pagination and sorting information
     * @return a page of user admin responses with statistics
     */
    Page<UserAdminResponse> getAllUsersWithStats(Pageable pageable);
}
