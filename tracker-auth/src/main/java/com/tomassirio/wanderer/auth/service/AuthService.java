package com.tomassirio.wanderer.auth.service;

import com.tomassirio.wanderer.auth.dto.LoginResponse;
import com.tomassirio.wanderer.auth.dto.RegisterRequest;
import java.util.UUID;

/**
 * Service interface for authentication operations. Provides methods for user login and
 * registration.
 *
 * @since 0.1.8
 */
public interface AuthService {

    /**
     * Authenticates a user with the provided username and password. If authentication is
     * successful, returns a JWT token and refresh token.
     *
     * @param username the username of the user attempting to log in
     * @param password the password of the user
     * @return a LoginResponse containing access token, refresh token, and metadata
     * @throws IllegalArgumentException if the credentials are invalid or the account is disabled
     * @throws IllegalStateException if there is an issue contacting the user query service
     */
    LoginResponse login(String username, String password);

    /**
     * Registers a new user with the provided registration details. Creates the user in the domain
     * via the command service and stores credentials in the auth database. If credential creation
     * fails after user creation, attempts to delete the created user as compensation.
     *
     * @param request the registration request containing username, email, and password
     * @return a LoginResponse containing the JWT token and metadata
     * @throws IllegalArgumentException if credentials already exist for the user
     * @throws IllegalStateException if user creation or credential saving fails, or if rollback
     *     fails
     */
    LoginResponse register(RegisterRequest request);

    /**
     * Logs out a user by blacklisting the provided JWT token and revoking all refresh tokens.
     *
     * @param token the JWT token to blacklist
     * @param userId the user ID (extracted from authenticated user)
     */
    void logout(String token, UUID userId);

    /**
     * Initiates a password reset by creating a reset token and returning it. In a production
     * environment, this token should be sent via email.
     *
     * @param email the email address of the user requesting password reset
     * @return the password reset token
     * @throws IllegalArgumentException if no user is found with the provided email
     */
    String initiatePasswordReset(String email);

    /**
     * Completes a password reset by validating the token and updating the user's password.
     *
     * @param token the password reset token
     * @param newPassword the new password
     * @throws IllegalArgumentException if the token is invalid, expired, or already used
     */
    void resetPassword(String token, String newPassword);

    /**
     * Changes a user's password after verifying the current password.
     *
     * @param userId the user ID
     * @param currentPassword the current password
     * @param newPassword the new password
     * @throws IllegalArgumentException if the current password is incorrect
     */
    void changePassword(UUID userId, String currentPassword, String newPassword);
}
