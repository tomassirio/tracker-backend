package com.tomassirio.wanderer.query.controller;

import com.tomassirio.wanderer.commons.constants.ApiConstants;
import com.tomassirio.wanderer.commons.security.CurrentUserId;
import com.tomassirio.wanderer.query.dto.UserAdminResponse;
import com.tomassirio.wanderer.query.dto.UserResponse;
import com.tomassirio.wanderer.query.service.UserQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
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
@RequestMapping(
        value = ApiConstants.USERS_PATH,
        produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "User Queries", description = "Endpoints for retrieving user information")
public class UserQueryController {

    private final UserQueryService userQueryService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get all users with statistics (Admin only)",
            description =
                    "Retrieves all users with their statistics (friends, followers, trips count). "
                            + "Use query parameters: page, size, sort (e.g., sort=username,asc)")
    @ApiResponse(responseCode = "200", description = "Users retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized - valid JWT required")
    @ApiResponse(responseCode = "403", description = "Forbidden - ADMIN role required")
    public ResponseEntity<Page<UserAdminResponse>> getAllUsers(
            @Parameter(description = "Pagination and sorting parameters")
                    @PageableDefault(size = 20, sort = "username")
                    Pageable pageable) {
        return ResponseEntity.ok(userQueryService.getAllUsersWithStats(pageable));
    }

    @GetMapping(ApiConstants.USER_BY_ID_ENDPOINT)
    @Operation(summary = "Get user by ID", description = "Retrieves a specific user by their ID")
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userQueryService.getUserById(id));
    }

    @GetMapping(ApiConstants.USERNAME_ENDPOINT)
    @Operation(summary = "Get user by username", description = "Retrieves a user by their username")
    public ResponseEntity<UserResponse> getUserByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userQueryService.getUserByUsername(username));
    }

    @GetMapping(ApiConstants.ME_SUFFIX)
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(
            summary = "Get current authenticated user's profile",
            description = "Retrieves the profile of the currently authenticated user")
    public ResponseEntity<UserResponse> getMyUser(
            @Parameter(hidden = true) @CurrentUserId UUID userId) {
        return ResponseEntity.ok(userQueryService.getUserById(userId));
    }
}
