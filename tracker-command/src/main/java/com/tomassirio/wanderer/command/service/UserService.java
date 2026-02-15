package com.tomassirio.wanderer.command.service;

import com.tomassirio.wanderer.command.controller.request.UserCreationRequest;
import java.util.UUID;

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
 *   <li>Output: UUID of the created user.
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
     * @return the UUID of the created user
     * @throws IllegalArgumentException if username or email are already in use
     */
    UUID createUser(UserCreationRequest request);
}
