package com.tomassirio.wanderer.auth.controller;

import com.tomassirio.wanderer.auth.dto.LoginRequest;
import com.tomassirio.wanderer.auth.dto.LoginResponse;
import com.tomassirio.wanderer.auth.dto.PasswordChangeRequest;
import com.tomassirio.wanderer.auth.dto.PasswordResetConfirmRequest;
import com.tomassirio.wanderer.auth.dto.PasswordResetRequest;
import com.tomassirio.wanderer.auth.dto.RefreshTokenRequest;
import com.tomassirio.wanderer.auth.dto.RefreshTokenResponse;
import com.tomassirio.wanderer.auth.dto.RegisterPendingResponse;
import com.tomassirio.wanderer.auth.dto.RegisterRequest;
import com.tomassirio.wanderer.auth.dto.VerifyEmailRequest;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for authentication operations. Handles user login, registration, logout, token
 * refresh, and password management.
 *
 * @since 0.1.8
 */
@RestController
@RequestMapping(value = ApiConstants.AUTH_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user authentication and registration")
public class AuthController {

    private final AuthService authService;
    private final TokenService tokenService;

    @PostMapping(value = ApiConstants.LOGIN_ENDPOINT, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "User login",
            description = "Authenticates a user and returns access and refresh tokens")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request.username(), request.password());
        return ResponseEntity.ok(response);
    }

    @PostMapping(
            value = ApiConstants.REGISTER_ENDPOINT,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "User registration",
            description =
                    "Initiates user registration by sending an email verification link. The user"
                            + " account is created only after email verification.")
    public ResponseEntity<RegisterPendingResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        RegisterPendingResponse response = authService.register(request);
        return ResponseEntity.status(202).body(response);
    }

    @PostMapping(
            value = ApiConstants.VERIFY_EMAIL_ENDPOINT,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Verify email",
            description =
                    "Verifies the user's email address using the token sent via email. Upon"
                            + " successful verification, creates the user account and returns access"
                            + " and refresh tokens.")
    public ResponseEntity<LoginResponse> verifyEmail(
            @Valid @RequestBody VerifyEmailRequest request) {
        LoginResponse response = authService.verifyEmail(request.token());
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping(value = ApiConstants.VERIFY_EMAIL_ENDPOINT, produces = MediaType.TEXT_HTML_VALUE)
    @Operation(
            summary = "Verify email via link",
            description =
                    "Verifies the user's email address via a clickable link from the verification"
                            + " email. Returns an HTML page with the result.")
    public ResponseEntity<String> verifyEmailViaLink(@RequestParam("token") String token) {
        try {
            authService.verifyEmail(token);
            return ResponseEntity.ok(
                    buildVerificationHtml(
                            "Email Verified!",
                            "Your email has been verified successfully. You can now log in to your"
                                    + " account.",
                            true));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(
                            buildVerificationHtml(
                                    "Verification Failed",
                                    "The verification link is invalid or has expired. Please"
                                            + " register again.",
                                    false));
        }
    }

    private String buildVerificationHtml(String title, String message, boolean success) {
        String color = success ? "#4CAF50" : "#f44336";
        String icon = success ? "✓" : "✗";
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>%s</title>
                    <style>
                        body { font-family: Arial, sans-serif; display: flex; justify-content: center;
                               align-items: center; min-height: 100vh; margin: 0; background: #f5f5f5; }
                        .card { background: white; border-radius: 8px; padding: 40px; text-align: center;
                                box-shadow: 0 2px 10px rgba(0,0,0,0.1); max-width: 400px; }
                        .icon { font-size: 64px; color: %s; }
                        h1 { color: #333; margin: 16px 0 8px; }
                        p { color: #666; line-height: 1.6; }
                    </style>
                </head>
                <body>
                    <div class="card">
                        <div class="icon">%s</div>
                        <h1>%s</h1>
                        <p>%s</p>
                    </div>
                </body>
                </html>
                """
                .formatted(title, color, icon, title, message);
    }

    @PostMapping(ApiConstants.LOGOUT_ENDPOINT)
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(
            summary = "User logout",
            description =
                    "Invalidates the current access token and all refresh tokens for the user",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<Map<String, String>> logout(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        authService.logout(userId);
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @PostMapping(value = ApiConstants.REFRESH_ENDPOINT, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Refresh access token",
            description = "Exchanges a refresh token for a new access token and refresh token")
    public ResponseEntity<RefreshTokenResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {
        RefreshTokenResponse response = tokenService.refreshAccessToken(request.refreshToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping(
            value = ApiConstants.PASSWORD_RESET_ENDPOINT,
            consumes = MediaType.APPLICATION_JSON_VALUE)
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

    @PutMapping(
            value = ApiConstants.PASSWORD_RESET_ENDPOINT,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Complete password reset",
            description = "Resets the password using a valid reset token")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody PasswordResetConfirmRequest request) {
        authService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }

    @PutMapping(
            value = ApiConstants.PASSWORD_CHANGE_ENDPOINT,
            consumes = MediaType.APPLICATION_JSON_VALUE)
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
