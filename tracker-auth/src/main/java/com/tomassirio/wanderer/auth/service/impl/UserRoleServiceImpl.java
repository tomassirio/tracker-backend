package com.tomassirio.wanderer.auth.service.impl;

import com.tomassirio.wanderer.auth.domain.Credential;
import com.tomassirio.wanderer.auth.repository.CredentialRepository;
import com.tomassirio.wanderer.auth.service.UserRoleService;
import com.tomassirio.wanderer.commons.security.Role;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for user role management operations.
 *
 * @since 0.5.2
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserRoleServiceImpl implements UserRoleService {

    private final CredentialRepository credentialRepository;

    @Override
    @Transactional
    public void promoteToAdmin(UUID userId) {
        log.info("Promoting user {} to admin", userId);

        Optional<Credential> maybeCredential = credentialRepository.findById(userId);
        if (maybeCredential.isEmpty()) {
            throw new IllegalArgumentException("User not found with ID: " + userId);
        }

        Credential credential = maybeCredential.get();

        if (credential.getRoles().contains(Role.ADMIN)) {
            throw new IllegalArgumentException("User already has admin role");
        }

        // Add ADMIN role while keeping existing roles
        Set<Role> updatedRoles = new HashSet<>(credential.getRoles());
        updatedRoles.add(Role.ADMIN);
        credential.setRoles(updatedRoles);

        credentialRepository.save(credential);
        log.info("User {} promoted to admin successfully", userId);
    }

    @Override
    @Transactional
    public void demoteFromAdmin(UUID userId) {
        log.info("Demoting user {} from admin", userId);

        Optional<Credential> maybeCredential = credentialRepository.findById(userId);
        if (maybeCredential.isEmpty()) {
            throw new IllegalArgumentException("User not found with ID: " + userId);
        }

        Credential credential = maybeCredential.get();

        if (!credential.getRoles().contains(Role.ADMIN)) {
            throw new IllegalArgumentException("User does not have admin role");
        }

        // Remove ADMIN role but keep USER role
        Set<Role> updatedRoles = new HashSet<>(credential.getRoles());
        updatedRoles.remove(Role.ADMIN);
        credential.setRoles(updatedRoles);

        credentialRepository.save(credential);
        log.info("User {} demoted from admin successfully", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Role> getUserRoles(UUID userId) {
        Optional<Credential> maybeCredential = credentialRepository.findById(userId);
        if (maybeCredential.isEmpty()) {
            throw new IllegalArgumentException("User not found with ID: " + userId);
        }

        return Set.copyOf(maybeCredential.get().getRoles());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAnyAdmins() {
        return credentialRepository.existsByRolesContaining("ADMIN");
    }
}
