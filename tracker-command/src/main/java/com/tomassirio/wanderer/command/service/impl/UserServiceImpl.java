package com.tomassirio.wanderer.command.service.impl;

import com.tomassirio.wanderer.command.client.TrackerAuthClient;
import com.tomassirio.wanderer.command.controller.request.UserCreationRequest;
import com.tomassirio.wanderer.command.event.UserCreatedEvent;
import com.tomassirio.wanderer.command.event.UserDeletedEvent;
import com.tomassirio.wanderer.command.repository.UserRepository;
import com.tomassirio.wanderer.command.service.UserService;
import com.tomassirio.wanderer.commons.domain.User;
import feign.FeignException;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final TrackerAuthClient trackerAuthClient;

    @Override
    public UUID createUser(UserCreationRequest request) {
        log.info("Creating user with username={} email={}", request.username(), request.email());

        log.debug("Checking username uniqueness for {}", request.username());
        Optional<User> byUsername = userRepository.findByUsername(request.username());
        if (byUsername.isPresent()) {
            log.warn("Username already in use: {}", request.username());
            throw new IllegalArgumentException("Username already in use");
        }

        // Pre-generate ID
        UUID userId = UUID.randomUUID();

        // Publish event - persistence handler will write to DB
        eventPublisher.publishEvent(
                UserCreatedEvent.builder().userId(userId).username(request.username()).build());

        log.info("User created with id={}", userId);
        return userId;
    }

    @Override
    public void deleteUser(UUID userId) {
        log.info("Deleting user with id={}", userId);

        userRepository
                .findById(userId)
                .orElseThrow(
                        () -> new EntityNotFoundException("User not found with id: " + userId));

        // Delete all local user data
        eventPublisher.publishEvent(UserDeletedEvent.builder().userId(userId).build());

        // Delete credentials from auth service (best-effort)
        try {
            trackerAuthClient.deleteCredentials(userId);
            log.info("Deleted credentials from auth service for user: {}", userId);
        } catch (FeignException.BadRequest | FeignException.NotFound e) {
            log.warn(
                    "User {} not found in auth service, skipping credential deletion: {}",
                    userId,
                    e.getMessage());
        } catch (Exception e) {
            log.error("Failed to delete credentials from auth service for user: {}", userId, e);
            throw new RuntimeException(
                    "Failed to delete credentials from auth service: " + e.getMessage(), e);
        }

        log.info("User deletion completed for id={}", userId);
    }

    @Override
    public void deleteUserData(UUID userId) {
        log.info("Deleting local data for user with id={}", userId);

        userRepository
                .findById(userId)
                .orElseThrow(
                        () -> new EntityNotFoundException("User not found with id: " + userId));

        eventPublisher.publishEvent(UserDeletedEvent.builder().userId(userId).build());

        log.info("User data deletion processed for id={}", userId);
    }
}
