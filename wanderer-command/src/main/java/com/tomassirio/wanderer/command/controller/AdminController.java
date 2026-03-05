package com.tomassirio.wanderer.command.controller;

import com.tomassirio.wanderer.command.service.AdminService;
import com.tomassirio.wanderer.command.service.UserService;
import com.tomassirio.wanderer.commons.constants.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for admin command operations. The frontend calls these endpoints for admin write
 * operations, which delegate to the auth service via Feign.
 *
 * <p>All endpoints in this controller require ADMIN role.
 *
 * @since 0.5.3
 */
@RestController
@RequestMapping(value = ApiConstants.ADMIN_USERS_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin", description = "Admin-only endpoints for user management (command side)")
public class AdminController {

    private final AdminService adminService;
    private final UserService userService;

    /**
     * Promotes a user to admin role.
     *
     * @param userId the ID of the user to promote
     * @return 204 No Content on success
     */
    @PostMapping(ApiConstants.ADMIN_USER_PROMOTE_ENDPOINT)
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
        adminService.promoteToAdmin(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Demotes a user from admin role.
     *
     * @param userId the ID of the user to demote
     * @return 204 No Content on success
     */
    @DeleteMapping(ApiConstants.ADMIN_USER_PROMOTE_ENDPOINT)
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
        adminService.demoteFromAdmin(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Deletes a user from the system.
     *
     * @param userId the ID of the user to delete
     * @return 204 No Content on success
     */
    @DeleteMapping(ApiConstants.ADMIN_USER_BY_ID_ENDPOINT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete user",
            description =
                    "Permanently deletes a user from the system, including all their data. "
                            + "Cannot delete the last admin user.")
    @ApiResponse(responseCode = "204", description = "User deleted successfully")
    @ApiResponse(
            responseCode = "400",
            description = "User not found or cannot delete last admin",
            content = @Content(schema = @Schema(implementation = Map.class)))
    @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - valid JWT required",
            content = @Content)
    @ApiResponse(
            responseCode = "403",
            description = "Forbidden - ADMIN role required",
            content = @Content)
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "User ID to delete", required = true) @PathVariable
                    UUID userId) {
        log.info("Admin deleting user {}", userId);
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
