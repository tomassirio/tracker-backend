package com.tomassirio.wanderer.auth.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.tomassirio.wanderer.auth.domain.Credential;
import com.tomassirio.wanderer.auth.repository.CredentialRepository;
import com.tomassirio.wanderer.auth.service.impl.AdminServiceImpl;
import com.tomassirio.wanderer.commons.security.Role;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock private CredentialRepository credentialRepository;

    @InjectMocks private AdminServiceImpl adminService;

    private UUID testUserId;
    private Credential testCredential;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testCredential =
                Credential.builder()
                        .userId(testUserId)
                        .email("test@example.com")
                        .passwordHash("hashedPassword")
                        .roles(new HashSet<>(Set.of(Role.USER)))
                        .enabled(true)
                        .build();
    }

    @Test
    void deleteCredentials_whenUserExists_shouldDeleteCredentials() {
        when(credentialRepository.findById(testUserId)).thenReturn(Optional.of(testCredential));

        adminService.deleteCredentials(testUserId);

        verify(credentialRepository).delete(testCredential);
    }

    @Test
    void deleteCredentials_whenUserNotFound_shouldThrowException() {
        when(credentialRepository.findById(testUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.deleteCredentials(testUserId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");

        verify(credentialRepository, never()).delete(any());
    }

    @Test
    void deleteCredentials_whenLastAdmin_shouldThrowException() {
        Credential adminCredential =
                Credential.builder()
                        .userId(testUserId)
                        .email("admin@example.com")
                        .passwordHash("hashedPassword")
                        .roles(new HashSet<>(Set.of(Role.USER, Role.ADMIN)))
                        .enabled(true)
                        .build();

        when(credentialRepository.findById(testUserId)).thenReturn(Optional.of(adminCredential));
        when(credentialRepository.findAll()).thenReturn(List.of(adminCredential));

        assertThatThrownBy(() -> adminService.deleteCredentials(testUserId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot delete the last admin user");

        verify(credentialRepository, never()).delete(any());
    }

    @Test
    void deleteCredentials_whenAdminButNotLast_shouldDeleteCredentials() {
        UUID otherAdminId = UUID.randomUUID();
        Credential adminCredential =
                Credential.builder()
                        .userId(testUserId)
                        .email("admin@example.com")
                        .passwordHash("hashedPassword")
                        .roles(new HashSet<>(Set.of(Role.USER, Role.ADMIN)))
                        .enabled(true)
                        .build();
        Credential otherAdminCredential =
                Credential.builder()
                        .userId(otherAdminId)
                        .email("admin2@example.com")
                        .passwordHash("hashedPassword")
                        .roles(new HashSet<>(Set.of(Role.USER, Role.ADMIN)))
                        .enabled(true)
                        .build();

        when(credentialRepository.findById(testUserId)).thenReturn(Optional.of(adminCredential));
        when(credentialRepository.findAll())
                .thenReturn(List.of(adminCredential, otherAdminCredential));

        adminService.deleteCredentials(testUserId);

        verify(credentialRepository).delete(adminCredential);
    }
}
