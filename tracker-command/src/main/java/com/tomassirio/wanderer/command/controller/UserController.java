package com.tomassirio.wanderer.command.controller;

import com.tomassirio.wanderer.command.dto.UserCreationRequest;
import com.tomassirio.wanderer.command.service.UserService;
import com.tomassirio.wanderer.commons.constants.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for user command operations. Handles user creation requests.
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
}
