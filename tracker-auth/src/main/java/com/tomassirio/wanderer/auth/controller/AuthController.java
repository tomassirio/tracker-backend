package com.tomassirio.wanderer.auth.controller;

import com.tomassirio.wanderer.auth.dto.LoginRequest;
import com.tomassirio.wanderer.auth.dto.LoginResponse;
import com.tomassirio.wanderer.auth.dto.PasswordChangeRequest;
import com.tomassirio.wanderer.auth.dto.PasswordResetConfirmRequest;
import com.tomassirio.wanderer.auth.dto.PasswordResetRequest;
import com.tomassirio.wanderer.auth.dto.RefreshTokenRequest;
import com.tomassirio.wanderer.auth.dto.RefreshTokenResponse;
import com.tomassirio.wanderer.auth.dto.RegisterRequest;
import com.tomassirio.wanderer.auth.service.AuthService;
import com.tomassirio.wanderer.auth.service.TokenService;
import com.tomassirio.wanderer.commons.constants.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for authentication operations. Handles user login, registration, logout, token
 * refresh, and password management.
 *
 * @since 0.1.8
 */
@RestController
@RequestMapping(ApiConstants.AUTH_PATH)
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user authentication and registration")
public class AuthController {

    private final AuthService authService;
    private final TokenService tokenService;

    @PostMapping(ApiConstants.LOGIN_ENDPOINT)
    @Operation(
            summary = "User login",
            description = "Authenticates a user and returns access and refresh tokens")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request.username(), request.password());
        return ResponseEntity.ok(response);
    }

    @PostMapping(ApiConstants.REGISTER_ENDPOINT)
    @Operation(
            summary = "User registration",
            description = "Registers a new user and returns access and refresh tokens")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        LoginResponse resp = authService.register(request);
        return ResponseEntity.status(201).body(resp);
    }

    @PostMapping(ApiConstants.LOGOUT_ENDPOINT)
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(
            summary = "User logout",
            description =
                    "Invalidates the current access token and all refresh tokens for the user",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<Map<String, String>> logout(
            @RequestHeader("Authorization") String authHeader, @AuthenticationPrincipal Jwt jwt) {
        // Extract token from Authorization header
        String token = authHeader.replace("Bearer ", "");
        UUID userId = UUID.fromString(jwt.getSubject());
        authService.logout(token, userId);
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @PostMapping(ApiConstants.REFRESH_ENDPOINT)
    @Operation(
            summary = "Refresh access token",
            description = "Exchanges a refresh token for a new access token and refresh token")
    public ResponseEntity<RefreshTokenResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {
        RefreshTokenResponse response = tokenService.refreshAccessToken(request.refreshToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping(ApiConstants.PASSWORD_RESET_ENDPOINT)
    @Operation(
            summary = "Initiate password reset",
            description =
                    "Sends a password reset token (in production, this would be sent via email)")
    public ResponseEntity<Map<String, String>> initiatePasswordReset(
            @Valid @RequestBody PasswordResetRequest request) {
        String resetToken = authService.initiatePasswordReset(request.email());
        // In production, this token should be sent via email instead of returned in the response
        return ResponseEntity.ok(
                Map.of("message", "Password reset token generated", "token", resetToken));
    }

    @PutMapping(ApiConstants.PASSWORD_RESET_ENDPOINT)
    @Operation(
            summary = "Complete password reset",
            description = "Resets the password using a valid reset token")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody PasswordResetConfirmRequest request) {
        authService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }

    @PutMapping(ApiConstants.PASSWORD_CHANGE_ENDPOINT)
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(
            summary = "Change password",
            description = "Changes the password for the authenticated user",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<Map<String, String>> changePassword(
            @Valid @RequestBody PasswordChangeRequest request, @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        authService.changePassword(userId, request.currentPassword(), request.newPassword());
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }
}
