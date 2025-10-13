package com.tomassirio.wanderer.query.service.impl;

import com.tomassirio.wanderer.query.dto.UserResponse;
import com.tomassirio.wanderer.query.repository.UserRepository;
import com.tomassirio.wanderer.query.service.UserQueryService;
import jakarta.persistence.EntityNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service implementation for user query operations. Handles user retrieval logic using the user
 * repository.
 *
 * @since 0.1.8
 */
@Service
@RequiredArgsConstructor
public class UserQueryServiceImpl implements UserQueryService {

    private final UserRepository userRepository;

    @Override
    public UserResponse getUserById(UUID id) {
        if (id == null) {
            // Defensive: if argument resolver didn't run or returned null, return 401 instead of
            // 500
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Missing or invalid authenticated user id");
        }
        var user =
                userRepository
                        .findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail());
    }

    @Override
    public UserResponse getUserByUsername(String username) {
        var user =
                userRepository
                        .findByUsername(username)
                        .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail());
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        var user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail());
    }
}
