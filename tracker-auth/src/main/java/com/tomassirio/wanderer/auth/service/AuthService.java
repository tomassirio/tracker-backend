package com.tomassirio.wanderer.auth.service;

import com.tomassirio.wanderer.auth.dto.LoginResponse;
import com.tomassirio.wanderer.auth.dto.RegisterRequest;

/**
 * Service interface for authentication operations.
 * Provides methods for user login and registration.
 *
 * @since 0.1.8
 */
public interface AuthService {

    /**
     * Authenticates a user with the provided username and password.
     * If authentication is successful, returns a JWT token.
     *
     * @param username the username of the user attempting to log in
     * @param password the password of the user
     * @return a JWT token as a String
     * @throws IllegalArgumentException if the credentials are invalid or the account is disabled
     * @throws IllegalStateException if there is an issue contacting the user query service
     */
    String login(String username, String password);

    /**
     * Registers a new user with the provided registration details.
     * Creates the user in the domain via the command service and stores credentials in the auth database.
     * If credential creation fails after user creation, attempts to delete the created user as compensation.
     *
     * @param request the registration request containing username, email, and password
     * @return a LoginResponse containing the JWT token and metadata
     * @throws IllegalArgumentException if credentials already exist for the user
     * @throws IllegalStateException if user creation or credential saving fails, or if rollback fails
     */
    LoginResponse register(RegisterRequest request);
}
