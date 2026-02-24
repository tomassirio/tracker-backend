package com.tomassirio.wanderer.auth.service.impl;

import com.tomassirio.wanderer.auth.domain.Credential;
import com.tomassirio.wanderer.auth.repository.CredentialRepository;
import com.tomassirio.wanderer.auth.service.AdminService;
import com.tomassirio.wanderer.commons.security.Role;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for admin operations. Handles credential deletion with last-admin
 * protection. Data cleanup is handled by the command service before calling these methods.
 *
 * @since 0.5.2
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final CredentialRepository credentialRepository;

    @Override
    @Transactional
    public void deleteCredentials(UUID userId) {
        Credential credential = findAndValidateDeletion(userId);
        credentialRepository.delete(credential);
        log.info("Deleted credentials for user: {}", userId);
    }

    private Credential findAndValidateDeletion(UUID userId) {
        Credential credential =
                credentialRepository
                        .findById(userId)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "User not found with id: " + userId));

        // Prevent deleting the last admin
        if (credential.getRoles().contains(Role.ADMIN)) {
            long adminCount =
                    credentialRepository.findAll().stream()
                            .filter(c -> c.getRoles().contains(Role.ADMIN))
                            .count();
            if (adminCount <= 1) {
                throw new IllegalStateException(
                        "Cannot delete the last admin user. Promote another user to admin first.");
            }
        }

        return credential;
    }
}
