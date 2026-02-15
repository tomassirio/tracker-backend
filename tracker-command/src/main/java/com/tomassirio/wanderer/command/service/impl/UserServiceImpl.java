package com.tomassirio.wanderer.command.service.impl;

import com.tomassirio.wanderer.command.controller.request.UserCreationRequest;
import com.tomassirio.wanderer.command.event.UserCreatedEvent;
import com.tomassirio.wanderer.command.repository.UserRepository;
import com.tomassirio.wanderer.command.service.UserService;
import com.tomassirio.wanderer.commons.domain.User;
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
}
