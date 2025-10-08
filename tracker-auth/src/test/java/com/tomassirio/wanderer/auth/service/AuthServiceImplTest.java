package com.tomassirio.wanderer.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
import feign.FeignException;
import feign.FeignException.NotFound;
import feign.Request;
import feign.RequestTemplate;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
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

    @Mock private TrackerCommandClient trackerCommandClient;

    @Mock private TrackerQueryClient trackerQueryClient;

    @InjectMocks private AuthServiceImpl authService;

    private User testUser;

    private Credential testCredential;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@example.com")
                .build();
        testCredential = Credential.builder()
                .userId(testUser.getId())
                .passwordHash("hashedPassword")
                .enabled(true)
                .build();
    }

    @Test
    void login_whenValidCredentials_shouldReturnToken() {
        String password = "password123";
        String token = "jwt.token";

        when(trackerQueryClient.getUserByUsername(testUser.getUsername())).thenReturn(testUser);
        when(credentialRepository.findById(testUser.getId())).thenReturn(Optional.of(testCredential));
        when(passwordEncoder.matches(password, testCredential.getPasswordHash())).thenReturn(true);
        when(jwtService.generateToken(testUser)).thenReturn(token);

        String result = authService.login(testUser.getUsername(), password);

        assertEquals(token, result);
        verify(jwtService).generateToken(testUser);
    }

    @Test
    void login_whenUserNotFound_shouldThrowIllegalArgumentException() {
        Request dummyRequest = Request.create(Request.HttpMethod.GET, "http://dummy", Map.of(), null, StandardCharsets.UTF_8);
        when(trackerQueryClient.getUserByUsername("nonexistent")).thenThrow(new NotFound("User not found", dummyRequest, null, null));

        assertThrows(IllegalArgumentException.class, () -> authService.login("nonexistent", "password"));
    }

    @Test
    void login_whenCredentialsNotFound_shouldThrowIllegalArgumentException() {
        when(trackerQueryClient.getUserByUsername(testUser.getUsername())).thenReturn(testUser);
        when(credentialRepository.findById(testUser.getId())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> authService.login(testUser.getUsername(), "password"));
    }

    @Test
    void login_whenAccountDisabled_shouldThrowIllegalArgumentException() {
        testCredential.setEnabled(false);

        when(trackerQueryClient.getUserByUsername(testUser.getUsername())).thenReturn(testUser);
        when(credentialRepository.findById(testUser.getId())).thenReturn(Optional.of(testCredential));

        assertThrows(IllegalArgumentException.class, () -> authService.login(testUser.getUsername(), "password"));
    }

    @Test
    void login_whenPasswordIncorrect_shouldThrowIllegalArgumentException() {
        when(trackerQueryClient.getUserByUsername(testUser.getUsername())).thenReturn(testUser);
        when(credentialRepository.findById(testUser.getId())).thenReturn(Optional.of(testCredential));
        when(passwordEncoder.matches("wrongpassword", testCredential.getPasswordHash())).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> authService.login(testUser.getUsername(), "wrongpassword"));
    }

    @Test
    void register_whenValidRequest_shouldReturnLoginResponse() {
        RegisterRequest request = new RegisterRequest("testuser", "test@example.com", "password123");
        String token = "jwt.token";
        long expiresIn = 3600000L;

        when(trackerCommandClient.createUser(any())).thenReturn(testUser);
        when(credentialRepository.findById(testUser.getId())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.password())).thenReturn("hashedPassword");
        when(jwtService.generateToken(testUser)).thenReturn(token);
        when(jwtService.getExpirationMs()).thenReturn(expiresIn);

        LoginResponse result = authService.register(request);

        assertEquals(token, result.accessToken());
        assertEquals("Bearer", result.tokenType());
        assertEquals(expiresIn, result.expiresIn());
        verify(credentialRepository).save(any(Credential.class));
        verify(trackerCommandClient, never()).deleteUser(any());
    }

    @Test
    void register_whenUserCreationFails_shouldThrowIllegalStateException() {
        RegisterRequest request = new RegisterRequest("testuser", "test@example.com", "password123");

        when(trackerCommandClient.createUser(any())).thenThrow(FeignException.class);

        assertThrows(IllegalStateException.class, () -> authService.register(request));
        verify(credentialRepository, never()).save(any());
    }

    @Test
    void register_whenCredentialsAlreadyExist_shouldThrowIllegalStateException() {
        RegisterRequest request = new RegisterRequest("testuser", "test@example.com", "password123");

        when(trackerCommandClient.createUser(any())).thenReturn(testUser);
        when(credentialRepository.findById(testUser.getId())).thenReturn(Optional.of(testCredential));

        assertThrows(IllegalStateException.class, () -> authService.register(request));
        verify(credentialRepository, never()).save(any());
        verify(trackerCommandClient).deleteUser(testUser.getId());
    }
}
