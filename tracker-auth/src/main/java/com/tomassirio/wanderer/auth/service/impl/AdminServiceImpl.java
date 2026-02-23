package com.tomassirio.wanderer.auth.service.impl;

import com.tomassirio.wanderer.auth.client.TrackerCommandClient;
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
 * Service implementation for admin operations.
 *
 * @since 0.5.2
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final CredentialRepository credentialRepository;
    private final TrackerCommandClient trackerCommandClient;

    @Override
    @Transactional
    public void deleteUser(UUID userId) {
        Credential credential =
                credentialRepository
                        .findById(userId)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "User not found with id: " + userId));

        // Prevent deleting the last admin
        if (credential.getRoles().contains(Role.ADMIN)) {
            boolean hasOtherAdmins = credentialRepository.existsByRolesContaining("ADMIN");
            long adminCount =
                    credentialRepository.findAll().stream()
                            .filter(c -> c.getRoles().contains(Role.ADMIN))
                            .count();
            if (adminCount <= 1) {
                throw new IllegalStateException(
                        "Cannot delete the last admin user. Promote another user to admin first.");
            }
        }

        // Delete from command service (user data, trips, etc.)
        try {
            trackerCommandClient.deleteUser(userId);
            log.info("Deleted user data from command service for user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to delete user data from command service for user: {}", userId, e);
            throw new RuntimeException(
                    "Failed to delete user data from command service: " + e.getMessage(), e);
        }

        // Delete credentials from auth service
        credentialRepository.delete(credential);
        log.info("Deleted credentials for user: {}", userId);
    }
}
