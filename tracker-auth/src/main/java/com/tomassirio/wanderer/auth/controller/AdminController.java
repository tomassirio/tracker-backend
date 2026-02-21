package com.tomassirio.wanderer.auth.controller;

import com.tomassirio.wanderer.auth.service.UserRoleService;
import com.tomassirio.wanderer.commons.security.Role;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for admin-specific operations. Provides endpoints for user role management.
 *
 * <p>All endpoints in this controller require ADMIN role.
 *
 * @since 0.5.2
 */
@RestController
@RequestMapping("/api/1/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin", description = "Admin-only endpoints for user role management")
public class AdminController {

    private final UserRoleService userRoleService;

    /**
     * Promotes a user to admin role.
     *
     * @param userId the ID of the user to promote
     * @return 204 No Content on success
     */
    @PostMapping("/users/{userId}/promote")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Promote user to admin",
            description = "Adds ADMIN role to the specified user. Only admins can promote users.")
    @ApiResponse(responseCode = "204", description = "User promoted successfully")
    @ApiResponse(
            responseCode = "400",
            description = "User not found or already has admin role",
            content = @Content(schema = @Schema(implementation = Map.class)))
    @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - valid JWT required",
            content = @Content)
    @ApiResponse(
            responseCode = "403",
            description = "Forbidden - ADMIN role required",
            content = @Content)
    public ResponseEntity<Void> promoteToAdmin(
            @Parameter(description = "User ID to promote", required = true) @PathVariable
                    UUID userId) {
        log.info("Admin promoting user {} to admin role", userId);
        userRoleService.promoteToAdmin(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Demotes a user from admin role.
     *
     * @param userId the ID of the user to demote
     * @return 204 No Content on success
     */
    @DeleteMapping("/users/{userId}/promote")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Demote user from admin",
            description =
                    "Removes ADMIN role from the specified user. Only admins can demote users.")
    @ApiResponse(responseCode = "204", description = "User demoted successfully")
    @ApiResponse(
            responseCode = "400",
            description = "User not found or doesn't have admin role",
            content = @Content(schema = @Schema(implementation = Map.class)))
    @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - valid JWT required",
            content = @Content)
    @ApiResponse(
            responseCode = "403",
            description = "Forbidden - ADMIN role required",
            content = @Content)
    public ResponseEntity<Void> demoteFromAdmin(
            @Parameter(description = "User ID to demote", required = true) @PathVariable
                    UUID userId) {
        log.info("Admin demoting user {} from admin role", userId);
        userRoleService.demoteFromAdmin(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Gets the roles assigned to a user.
     *
     * @param userId the ID of the user
     * @return the set of roles assigned to the user
     */
    @GetMapping("/users/{userId}/roles")
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
        Set<Role> roles = userRoleService.getUserRoles(userId);
        return ResponseEntity.ok(roles);
    }
}
