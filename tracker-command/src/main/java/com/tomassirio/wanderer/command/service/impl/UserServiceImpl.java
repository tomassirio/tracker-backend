package com.tomassirio.wanderer.command.service.impl;

import com.tomassirio.wanderer.command.dto.UserCreationRequest;
import com.tomassirio.wanderer.command.dto.UserResponse;
import com.tomassirio.wanderer.command.repository.UserRepository;
import com.tomassirio.wanderer.command.service.UserService;
import com.tomassirio.wanderer.commons.domain.User;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserResponse createUser(UserCreationRequest request) {
        log.info("Creating user with username={} email={}", request.username(), request.email());

        log.debug("Checking username uniqueness for {}", request.username());
        Optional<User> byUsername = userRepository.findByUsername(request.username());
        if (byUsername.isPresent()) {
            log.warn("Username already in use: {}", request.username());
            throw new IllegalArgumentException("Username already in use");
        }

        log.debug("Checking email uniqueness for {}", request.email());
        Optional<User> byEmail = userRepository.findByEmail(request.email());
        if (byEmail.isPresent()) {
            log.warn("Email already in use: {}", request.email());
            throw new IllegalArgumentException("Email already in use");
        }

        User user = User.builder().username(request.username()).email(request.email()).build();
        User saved = userRepository.save(user);

        log.info("User created with id={}", saved.getId());
        return new UserResponse(saved.getId(), saved.getUsername(), saved.getEmail());
    }
}
