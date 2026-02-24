package com.tomassirio.wanderer.command.controller;

import com.tomassirio.wanderer.command.controller.request.UserCreationRequest;
import com.tomassirio.wanderer.command.service.UserService;
import com.tomassirio.wanderer.commons.constants.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for user command operations. Handles user creation and deletion requests.
 *
 * @since 0.1.8
 */
@RestController
@RequestMapping(ApiConstants.USERS_PATH)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Users", description = "Endpoints for managing users")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(
            summary = "Create a new user",
            description =
                    "Registers a new user in the system. Returns 202 Accepted with the user ID as the operation completes asynchronously.")
    public ResponseEntity<UUID> createUser(@Valid @RequestBody UserCreationRequest request) {
        log.info("Received request to create user: {}", request.username());
        UUID userId = userService.createUser(request);
        log.info("Accepted user creation request with ID: {}", userId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(userId);
    }

    @DeleteMapping(ApiConstants.USER_BY_ID_ENDPOINT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete a user",
            description =
                    "Deletes a user and all associated data (trips, comments, friendships, follows). "
                            + "Only admins can delete users. Called by the auth service during admin user deletion.")
    @ApiResponse(responseCode = "202", description = "User deletion accepted")
    @ApiResponse(responseCode = "401", description = "Unauthorized - valid JWT required")
    @ApiResponse(responseCode = "403", description = "Forbidden - ADMIN role required")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "User ID to delete", required = true) @PathVariable UUID id) {
        log.info("Received request to delete user: {}", id);
        userService.deleteUser(id);
        log.info("Accepted user deletion request for ID: {}", id);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
