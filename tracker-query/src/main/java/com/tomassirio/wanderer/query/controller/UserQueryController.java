package com.tomassirio.wanderer.query.controller;

import com.tomassirio.wanderer.commons.security.CurrentUserId;
import com.tomassirio.wanderer.query.dto.UserResponse;
import com.tomassirio.wanderer.query.service.UserQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for user query operations. Handles user retrieval requests.
 *
 * @since 0.1.8
 */
@RestController
@RequestMapping("/api/1/users")
@RequiredArgsConstructor
public class UserQueryController {

    private final UserQueryService userQueryService;

    @GetMapping("/{id:[0-9a-fA-F\\-]{36}}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userQueryService.getUserById(id));
    }

    @GetMapping("/username/{username}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<UserResponse> getUserByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userQueryService.getUserByUsername(username));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(summary = "Get current authenticated user's profile")
    public ResponseEntity<UserResponse> getMyUser(
            @Parameter(hidden = true) @CurrentUserId UUID userId) {
        return ResponseEntity.ok(userQueryService.getUserById(userId));
    }
}
