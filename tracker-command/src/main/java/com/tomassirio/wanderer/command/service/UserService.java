package com.tomassirio.wanderer.command.service;

import com.tomassirio.wanderer.command.dto.UserCreationRequest;
import com.tomassirio.wanderer.command.dto.UserResponse;

/**
 * Service responsible for handling user write operations (command side).
 *
 * <p>Responsibilities:
 *
 * <ul>
 *   <li>Create users (ensuring username and email uniqueness).
 *   <li>Any command-side user mutations belong here.
 * </ul>
 *
 * <p>Contracts:
 *
 * <ul>
 *   <li>Input: {@link UserCreationRequest} containing username and email (validated by the
 *       controller).
 *   <li>Output: {@link UserResponse} with created user's id, username and email.
 *   <li>Error modes: throws {@link IllegalArgumentException} when username or email are already in
 *       use.
 * </ul>
 *
 * * @author tomassirio * @since 0.1.8
 */
public interface UserService {

    /**
     * Create a new user from the provided request.
     *
     * @param request validated creation request containing username and email
     * @return a {@link UserResponse} containing the created user's id, username and email
     * @throws IllegalArgumentException if username or email are already in use
     */
    UserResponse createUser(UserCreationRequest request);
}
