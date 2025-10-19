package com.tomassirio.wanderer.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tomassirio.wanderer.auth.client.TrackerCommandClient;
import com.tomassirio.wanderer.auth.client.TrackerQueryClient;
import com.tomassirio.wanderer.auth.domain.Credential;
import com.tomassirio.wanderer.auth.dto.LoginResponse;
import com.tomassirio.wanderer.auth.dto.RegisterRequest;
import com.tomassirio.wanderer.auth.repository.CredentialRepository;
import com.tomassirio.wanderer.auth.service.impl.AuthServiceImpl;
import com.tomassirio.wanderer.commons.domain.User;
import com.tomassirio.wanderer.commons.security.Role;
import feign.FeignException;
import feign.FeignException.NotFound;
import feign.Request;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private CredentialRepository credentialRepository;

    @Mock private PasswordEncoder passwordEncoder;

    @Mock private JwtService jwtService;

    @Mock private TokenService tokenService;

    @Mock private TrackerCommandClient trackerCommandClient;

    @Mock private TrackerQueryClient trackerQueryClient;

    @InjectMocks private AuthServiceImpl authService;

    private User testUser;

    private Credential testCredential;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(UUID.randomUUID()).username("testuser").build();
        testCredential =
                Credential.builder()
                        .userId(testUser.getId())
                        .passwordHash("hashedPassword")
                        .enabled(true)
                        .email("user@email.com")
                        .roles(Set.of(Role.USER))
                        .build();
    }

    @Test
    void login_whenValidCredentials_shouldReturnLoginResponse() {
        String password = "password123";
        String accessToken = "jwt.access.token";
        String refreshToken = "refresh.token";
        long expiresIn = 3600000L;

        when(trackerQueryClient.getUserByUsername(testUser.getUsername())).thenReturn(testUser);
        when(credentialRepository.findById(testUser.getId()))
                .thenReturn(Optional.of(testCredential));
        when(passwordEncoder.matches(password, testCredential.getPasswordHash())).thenReturn(true);
        when(jwtService.generateTokenWithJti(any(), any())).thenReturn(accessToken);
        when(tokenService.createRefreshToken(testUser.getId())).thenReturn(refreshToken);
        when(jwtService.getExpirationMs()).thenReturn(expiresIn);

        LoginResponse result = authService.login(testUser.getUsername(), password);

        assertEquals(accessToken, result.accessToken());
        assertEquals(refreshToken, result.refreshToken());
        assertEquals("Bearer", result.tokenType());
        assertEquals(expiresIn, result.expiresIn());
        verify(jwtService).generateTokenWithJti(any(), any());
        verify(tokenService).createRefreshToken(testUser.getId());
    }

    @Test
    void login_whenUserNotFound_shouldThrowIllegalArgumentException() {
        Request dummyRequest =
                Request.create(
                        Request.HttpMethod.GET,
                        "http://dummy",
                        Map.of(),
                        null,
                        StandardCharsets.UTF_8,
                        null);
        when(trackerQueryClient.getUserByUsername("nonexistent"))
                .thenThrow(new NotFound("User not found", dummyRequest, null, null));

        assertThrows(
                IllegalArgumentException.class, () -> authService.login("nonexistent", "password"));
    }

    @Test
    void login_whenCredentialsNotFound_shouldThrowIllegalArgumentException() {
        when(trackerQueryClient.getUserByUsername(testUser.getUsername())).thenReturn(testUser);
        when(credentialRepository.findById(testUser.getId())).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(testUser.getUsername(), "password"));
    }

    @Test
    void login_whenAccountDisabled_shouldThrowIllegalArgumentException() {
        testCredential.setEnabled(false);

        when(trackerQueryClient.getUserByUsername(testUser.getUsername())).thenReturn(testUser);
        when(credentialRepository.findById(testUser.getId()))
                .thenReturn(Optional.of(testCredential));

        assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(testUser.getUsername(), "password"));
    }

    @Test
    void login_whenPasswordIncorrect_shouldThrowIllegalArgumentException() {
        when(trackerQueryClient.getUserByUsername(testUser.getUsername())).thenReturn(testUser);
        when(credentialRepository.findById(testUser.getId()))
                .thenReturn(Optional.of(testCredential));
        when(passwordEncoder.matches("wrongpassword", testCredential.getPasswordHash()))
                .thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(testUser.getUsername(), "wrongpassword"));
    }

    @Test
    void register_whenValidRequest_shouldReturnLoginResponse() {
        RegisterRequest request =
                new RegisterRequest("testuser", "test@example.com", "password123");
        String accessToken = "jwt.access.token";
        String refreshToken = "refresh.token";
        long expiresIn = 3600000L;

        when(trackerCommandClient.createUser(any())).thenReturn(testUser);
        when(credentialRepository.findById(testUser.getId())).thenReturn(Optional.empty());
        when(credentialRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.password())).thenReturn("hashedPassword");
        when(jwtService.generateTokenWithJti(any(), any())).thenReturn(accessToken);
        when(tokenService.createRefreshToken(testUser.getId())).thenReturn(refreshToken);
        when(jwtService.getExpirationMs()).thenReturn(expiresIn);

        LoginResponse result = authService.register(request);

        assertEquals(accessToken, result.accessToken());
        assertEquals(refreshToken, result.refreshToken());
        assertEquals("Bearer", result.tokenType());
        assertEquals(expiresIn, result.expiresIn());
        verify(credentialRepository).save(any(Credential.class));
        verify(trackerCommandClient, never()).deleteUser(any());
    }

    @Test
    void register_whenUserCreationFails_shouldThrowIllegalStateException() {
        RegisterRequest request =
                new RegisterRequest("testuser", "test@example.com", "password123");

        when(trackerCommandClient.createUser(any())).thenThrow(FeignException.class);

        assertThrows(IllegalStateException.class, () -> authService.register(request));
        verify(credentialRepository, never()).save(any());
    }

    @Test
    void register_whenCredentialsAlreadyExist_shouldThrowIllegalStateException() {
        RegisterRequest request =
                new RegisterRequest("testuser", "test@example.com", "password123");

        when(trackerCommandClient.createUser(any())).thenReturn(testUser);
        when(credentialRepository.findById(testUser.getId()))
                .thenReturn(Optional.of(testCredential));

        assertThrows(IllegalStateException.class, () -> authService.register(request));
        verify(credentialRepository, never()).save(any());
        verify(trackerCommandClient).deleteUser(testUser.getId());
    }

    @Test
    void register_whenEmailAlreadyExists_shouldThrowIllegalStateException() {
        RegisterRequest request =
                new RegisterRequest("newuser", testCredential.getEmail(), "password123");

        when(trackerCommandClient.createUser(any())).thenReturn(testUser);
        when(credentialRepository.findById(testUser.getId())).thenReturn(Optional.empty());
        when(credentialRepository.findByEmail(testCredential.getEmail()))
                .thenReturn(Optional.of(testCredential));

        assertThrows(IllegalStateException.class, () -> authService.register(request));
        verify(credentialRepository, never()).save(any());
        verify(trackerCommandClient).deleteUser(testUser.getId());
    }

    @Test
    void logout_shouldRevokeRefreshTokens() {
        authService.logout(testUser.getId());

        verify(tokenService).revokeAllRefreshTokensForUser(testUser.getId());
    }

    @Test
    void initiatePasswordReset_whenEmailExists_shouldReturnResetToken() {
        String email = "user@email.com";
        String resetToken = "reset.token";

        when(credentialRepository.findByEmail(email)).thenReturn(Optional.of(testCredential));
        when(tokenService.createPasswordResetToken(testCredential.getUserId()))
                .thenReturn(resetToken);

        String result = authService.initiatePasswordReset(email);

        assertEquals(resetToken, result);
        verify(tokenService).createPasswordResetToken(testCredential.getUserId());
    }

    @Test
    void initiatePasswordReset_whenEmailNotFound_shouldThrowException() {
        String email = "nonexistent@email.com";

        when(credentialRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class, () -> authService.initiatePasswordReset(email));
    }

    @Test
    void resetPassword_whenValidToken_shouldUpdatePassword() {
        String token = "reset.token";
        String newPassword = "newPassword123";
        UUID userId = testUser.getId();

        when(tokenService.validatePasswordResetToken(token)).thenReturn(userId);
        when(credentialRepository.findById(userId)).thenReturn(Optional.of(testCredential));
        when(passwordEncoder.encode(newPassword)).thenReturn("hashedNewPassword");

        authService.resetPassword(token, newPassword);

        verify(credentialRepository).save(testCredential);
        verify(tokenService).markPasswordResetTokenAsUsed(token);
        verify(tokenService).revokeAllRefreshTokensForUser(userId);
    }

    @Test
    void resetPassword_whenCredentialNotFound_shouldThrowException() {
        String token = "reset.token";
        String newPassword = "newPassword123";
        UUID userId = UUID.randomUUID();

        when(tokenService.validatePasswordResetToken(token)).thenReturn(userId);
        when(credentialRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(
                IllegalStateException.class, () -> authService.resetPassword(token, newPassword));
    }

    @Test
    void changePassword_whenValidCurrentPassword_shouldUpdatePassword() {
        String currentPassword = "currentPassword";
        String newPassword = "newPassword123";
        UUID userId = testUser.getId();

        when(credentialRepository.findById(userId)).thenReturn(Optional.of(testCredential));
        when(passwordEncoder.matches(currentPassword, testCredential.getPasswordHash()))
                .thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn("hashedNewPassword");

        authService.changePassword(userId, currentPassword, newPassword);

        verify(credentialRepository).save(testCredential);
        verify(tokenService).revokeAllRefreshTokensForUser(userId);
    }

    @Test
    void changePassword_whenInvalidCurrentPassword_shouldThrowException() {
        String currentPassword = "wrongPassword";
        String newPassword = "newPassword123";
        UUID userId = testUser.getId();

        when(credentialRepository.findById(userId)).thenReturn(Optional.of(testCredential));
        when(passwordEncoder.matches(currentPassword, testCredential.getPasswordHash()))
                .thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> authService.changePassword(userId, currentPassword, newPassword));
        verify(credentialRepository, never()).save(any());
    }

    @Test
    void changePassword_whenCredentialNotFound_shouldThrowException() {
        String currentPassword = "currentPassword";
        String newPassword = "newPassword123";
        UUID userId = UUID.randomUUID();

        when(credentialRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> authService.changePassword(userId, currentPassword, newPassword));
    }

    @Test
    void login_whenUserReturnsNull_shouldThrowIllegalArgumentException() {
        // Test the null check after successful FeignClient call
        when(trackerQueryClient.getUserByUsername("testuser")).thenReturn(null);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> authService.login("testuser", "password"));

        assertEquals("Invalid credentials", exception.getMessage());
        verify(credentialRepository, never()).findById(any());
    }

    @Test
    void login_whenFeignExceptionNon404_shouldThrowIllegalStateException() {
        // Test FeignException with status code other than 404 (e.g., 500, 503)
        Request dummyRequest =
                Request.create(
                        Request.HttpMethod.GET,
                        "http://dummy",
                        Map.of(),
                        null,
                        StandardCharsets.UTF_8,
                        null);
        FeignException.InternalServerError serverError =
                new FeignException.InternalServerError(
                        "Internal Server Error", dummyRequest, null, null);

        when(trackerQueryClient.getUserByUsername("testuser")).thenThrow(serverError);

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> authService.login("testuser", "password"));

        assertEquals("Failed to contact user query service", exception.getMessage());
        assertEquals(serverError, exception.getCause());
        verify(credentialRepository, never()).findById(any());
    }

    @Test
    void register_whenCredentialSaveFailsAndRollbackFails_shouldThrowCompositeException() {
        RegisterRequest request =
                new RegisterRequest("testuser", "test@example.com", "password123");

        // User creation succeeds
        when(trackerCommandClient.createUser(any())).thenReturn(testUser);
        when(credentialRepository.findById(testUser.getId())).thenReturn(Optional.empty());
        when(credentialRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.password())).thenReturn("hashedPassword");

        // Credential save fails
        when(credentialRepository.save(any(Credential.class)))
                .thenThrow(new RuntimeException("Database error"));

        // Rollback (delete user) also fails - use doThrow for void methods
        Request dummyRequest =
                Request.create(
                        Request.HttpMethod.DELETE,
                        "http://dummy",
                        Map.of(),
                        null,
                        StandardCharsets.UTF_8,
                        null);
        doThrow(new FeignException.InternalServerError("Delete failed", dummyRequest, null, null))
                .when(trackerCommandClient)
                .deleteUser(testUser.getId());

        IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> authService.register(request));

        assertEquals(
                "Failed to create credentials and failed to rollback user creation: Delete failed",
                exception.getMessage());
        verify(trackerCommandClient).deleteUser(testUser.getId());
    }

    @Test
    void register_whenCredentialSaveFailsButRollbackSucceeds_shouldThrowIllegalStateException() {
        RegisterRequest request =
                new RegisterRequest("testuser", "test@example.com", "password123");

        // User creation succeeds
        when(trackerCommandClient.createUser(any())).thenReturn(testUser);
        when(credentialRepository.findById(testUser.getId())).thenReturn(Optional.empty());
        when(credentialRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.password())).thenReturn("hashedPassword");

        // Credential save fails
        when(credentialRepository.save(any(Credential.class)))
                .thenThrow(new RuntimeException("Database error"));

        IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> authService.register(request));

        assertEquals(
                "Failed to create credentials, rolled back user creation", exception.getMessage());
        verify(trackerCommandClient).deleteUser(testUser.getId());
    }
}
