package com.tomassirio.wanderer.command.service.impl;

import com.tomassirio.wanderer.command.client.TrackerAuthClient;
import com.tomassirio.wanderer.command.service.AdminService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service implementation for admin operations. Handles role management via auth Feign client.
 *
 * @since 0.5.3
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final TrackerAuthClient trackerAuthClient;

    @Override
    public void promoteToAdmin(UUID userId) {
        log.info("Promoting user {} to admin via auth service", userId);
        trackerAuthClient.promoteToAdmin(userId);
        log.info("User {} promoted to admin successfully", userId);
    }

    @Override
    public void demoteFromAdmin(UUID userId) {
        log.info("Demoting user {} from admin via auth service", userId);
        trackerAuthClient.demoteFromAdmin(userId);
        log.info("User {} demoted from admin successfully", userId);
    }
}
