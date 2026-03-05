package com.tomassirio.wanderer.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tomassirio.wanderer.auth.domain.Credential;
import com.tomassirio.wanderer.auth.repository.CredentialRepository;
import com.tomassirio.wanderer.auth.service.impl.UserRoleServiceImpl;
import com.tomassirio.wanderer.commons.security.Role;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserRoleServiceImplTest {

    @Mock private CredentialRepository credentialRepository;

    @InjectMocks private UserRoleServiceImpl userRoleService;

    private UUID testUserId;
    private Credential testCredential;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testCredential =
                Credential.builder()
                        .userId(testUserId)
                        .passwordHash("hashedPassword")
                        .email("test@example.com")
                        .enabled(true)
                        .roles(Set.of(Role.USER))
                        .build();
    }

    @Test
    void promoteToAdmin_whenUserExists_shouldAddAdminRole() {
        // Given
        when(credentialRepository.findById(testUserId)).thenReturn(Optional.of(testCredential));

        // When
        userRoleService.promoteToAdmin(testUserId);

        // Then
        ArgumentCaptor<Credential> credentialCaptor = ArgumentCaptor.forClass(Credential.class);
        verify(credentialRepository).save(credentialCaptor.capture());

        Credential savedCredential = credentialCaptor.getValue();
        assertThat(savedCredential.getRoles()).containsExactlyInAnyOrder(Role.USER, Role.ADMIN);
    }

    @Test
    void promoteToAdmin_whenUserNotFound_shouldThrowException() {
        // Given
        when(credentialRepository.findById(testUserId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userRoleService.promoteToAdmin(testUserId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void promoteToAdmin_whenUserAlreadyAdmin_shouldThrowException() {
        // Given
        testCredential.setRoles(Set.of(Role.USER, Role.ADMIN));
        when(credentialRepository.findById(testUserId)).thenReturn(Optional.of(testCredential));

        // When & Then
        assertThatThrownBy(() -> userRoleService.promoteToAdmin(testUserId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already has admin role");
    }

    @Test
    void demoteFromAdmin_whenUserHasAdminRole_shouldRemoveAdminRole() {
        // Given
        testCredential.setRoles(Set.of(Role.USER, Role.ADMIN));
        when(credentialRepository.findById(testUserId)).thenReturn(Optional.of(testCredential));

        // When
        userRoleService.demoteFromAdmin(testUserId);

        // Then
        ArgumentCaptor<Credential> credentialCaptor = ArgumentCaptor.forClass(Credential.class);
        verify(credentialRepository).save(credentialCaptor.capture());

        Credential savedCredential = credentialCaptor.getValue();
        assertThat(savedCredential.getRoles()).containsExactly(Role.USER);
        assertThat(savedCredential.getRoles()).doesNotContain(Role.ADMIN);
    }

    @Test
    void demoteFromAdmin_whenUserNotFound_shouldThrowException() {
        // Given
        when(credentialRepository.findById(testUserId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userRoleService.demoteFromAdmin(testUserId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void demoteFromAdmin_whenUserNotAdmin_shouldThrowException() {
        // Given
        when(credentialRepository.findById(testUserId)).thenReturn(Optional.of(testCredential));

        // When & Then
        assertThatThrownBy(() -> userRoleService.demoteFromAdmin(testUserId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not have admin role");
    }

    @Test
    void getUserRoles_whenUserExists_shouldReturnRoles() {
        // Given
        when(credentialRepository.findById(testUserId)).thenReturn(Optional.of(testCredential));

        // When
        Set<Role> roles = userRoleService.getUserRoles(testUserId);

        // Then
        assertThat(roles).containsExactly(Role.USER);
    }

    @Test
    void getUserRoles_whenUserHasMultipleRoles_shouldReturnAllRoles() {
        // Given
        testCredential.setRoles(Set.of(Role.USER, Role.ADMIN));
        when(credentialRepository.findById(testUserId)).thenReturn(Optional.of(testCredential));

        // When
        Set<Role> roles = userRoleService.getUserRoles(testUserId);

        // Then
        assertThat(roles).containsExactlyInAnyOrder(Role.USER, Role.ADMIN);
    }

    @Test
    void getUserRoles_whenUserNotFound_shouldThrowException() {
        // Given
        when(credentialRepository.findById(testUserId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userRoleService.getUserRoles(testUserId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void hasAnyAdmins_whenAdminsExist_shouldReturnTrue() {
        // Given
        when(credentialRepository.existsByRolesContaining("ADMIN")).thenReturn(true);

        // When
        boolean hasAdmins = userRoleService.hasAnyAdmins();

        // Then
        assertThat(hasAdmins).isTrue();
    }

    @Test
    void hasAnyAdmins_whenNoAdminsExist_shouldReturnFalse() {
        // Given
        when(credentialRepository.existsByRolesContaining("ADMIN")).thenReturn(false);

        // When
        boolean hasAdmins = userRoleService.hasAnyAdmins();

        // Then
        assertThat(hasAdmins).isFalse();
    }
}
