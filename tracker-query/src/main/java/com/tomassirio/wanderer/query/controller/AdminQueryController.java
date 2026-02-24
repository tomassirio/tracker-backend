package com.tomassirio.wanderer.query.controller;

import com.tomassirio.wanderer.commons.constants.ApiConstants;
import com.tomassirio.wanderer.commons.security.Role;
import com.tomassirio.wanderer.query.client.TrackerAuthClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for admin query operations. The frontend calls these endpoints for admin read
 * operations, which delegate to the auth service via Feign.
 *
 * <p>All endpoints in this controller require ADMIN role.
 *
 * @since 0.5.3
 */
@RestController
@RequestMapping(ApiConstants.ADMIN_USERS_PATH)
@RequiredArgsConstructor
@Slf4j
@Tag(
        name = "Admin Queries",
        description = "Admin-only endpoints for user role queries (query side)")
public class AdminQueryController {

    private final TrackerAuthClient trackerAuthClient;

    /**
     * Gets the roles assigned to a user.
     *
     * @param userId the ID of the user
     * @return the set of roles assigned to the user
     */
    @GetMapping(ApiConstants.ADMIN_USER_ROLES_ENDPOINT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get user roles",
            description =
                    "Retrieves the roles assigned to a specific user. Only admins can view user roles.")
    @ApiResponse(
            responseCode = "200",
            description = "Roles retrieved successfully",
            content = @Content(schema = @Schema(implementation = Set.class)))
    @ApiResponse(
            responseCode = "400",
            description = "User not found",
            content = @Content(schema = @Schema(implementation = Map.class)))
    @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - valid JWT required",
            content = @Content)
    @ApiResponse(
            responseCode = "403",
            description = "Forbidden - ADMIN role required",
            content = @Content)
    public ResponseEntity<Set<Role>> getUserRoles(
            @Parameter(description = "User ID", required = true) @PathVariable UUID userId) {
        log.info("Admin retrieving roles for user {}", userId);
        Set<Role> roles = trackerAuthClient.getUserRoles(userId);
        return ResponseEntity.ok(roles);
    }
}
