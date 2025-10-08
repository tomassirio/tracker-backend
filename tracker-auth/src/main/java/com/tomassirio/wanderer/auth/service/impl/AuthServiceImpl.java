package com.tomassirio.wanderer.auth.service.impl;

import com.tomassirio.wanderer.auth.client.TrackerCommandClient;
import com.tomassirio.wanderer.auth.client.TrackerQueryClient;
import com.tomassirio.wanderer.auth.domain.Credential;
import com.tomassirio.wanderer.auth.dto.LoginResponse;
import com.tomassirio.wanderer.auth.dto.RegisterRequest;
import com.tomassirio.wanderer.auth.repository.CredentialRepository;
import com.tomassirio.wanderer.auth.service.AuthService;
import com.tomassirio.wanderer.auth.service.JwtService;
import com.tomassirio.wanderer.commons.domain.User;
import feign.FeignException;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service implementation for authentication operations. Handles user login and registration using
 * Feign clients for inter-service communication.
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final CredentialRepository credentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TrackerCommandClient trackerCommandClient;
    private final TrackerQueryClient trackerQueryClient;

    /**
     * Verify credentials and return a JWT when valid.
     *
     * @throws IllegalArgumentException when credentials are invalid
     */
    public String login(String username, String password) {
        // Lookup user via query service (read side)
        User user;
        try {
            user = trackerQueryClient.getUserByUsername(username);
        } catch (FeignException e) {
            if (e.status() == 404) {
                throw new IllegalArgumentException("Invalid credentials");
            } else {
                throw new IllegalStateException("Failed to contact user query service", e);
            }
        }

        if (user == null) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        // Find credentials by user id in the auth database
        Optional<Credential> maybeCred = credentialRepository.findById(user.getId());
        if (maybeCred.isEmpty()) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        Credential cred = maybeCred.get();

        if (!cred.isEnabled()) {
            throw new IllegalArgumentException("Account disabled");
        }

        if (!passwordEncoder.matches(password, cred.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        return jwtService.generateToken(user);
    }

    /**
     * Register a new user and create credentials in the auth DB, then return a JWT. If credential
     * creation fails after the domain user was created, attempt to delete the created domain user
     * as a compensation step to avoid dangling accounts.
     */
    public LoginResponse register(RegisterRequest request) {
        // 1) Create the domain user via the command service
        var payload = Map.of("username", request.username(), "email", request.email());
        User createdUser;
        try {
            createdUser = trackerCommandClient.createUser(payload);
        } catch (FeignException e) {
            throw new IllegalStateException("Failed to create user in command service", e);
        }

        // 2) Create credential in auth DB — wrap in try/catch and compensate on failure
        try {
            if (credentialRepository.findById(createdUser.getId()).isPresent()) {
                throw new IllegalArgumentException(
                        "Credentials already exist for user: " + createdUser.getId());
            }
            String hash = passwordEncoder.encode(request.password());
            Credential credential =
                    Credential.builder()
                            .userId(createdUser.getId())
                            .passwordHash(hash)
                            .enabled(true)
                            .build();
            credentialRepository.save(credential);
        } catch (Exception e) {
            // Attempt to delete the created domain user as compensation
            try {
                trackerCommandClient.deleteUser(createdUser.getId());
            } catch (FeignException ex) {
                // Log and swallow the delete failure — we'll still rethrow the original exception
                // (Logging framework may be added; for now throw a composed exception)
                throw new IllegalStateException(
                        "Failed to create credentials and failed to rollback user creation: "
                                + ex.getMessage(),
                        e);
            }
            throw new IllegalStateException(
                    "Failed to create credentials, rolled back user creation", e);
        }

        // 3) Issue JWT
        String token = jwtService.generateToken(createdUser);
        return new LoginResponse(token, "Bearer", jwtService.getExpirationMs());
    }
}
