package com.tomassirio.wanderer.query.controller;

import com.tomassirio.wanderer.query.dto.UserResponse;
import com.tomassirio.wanderer.query.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for user query operations.
 * Handles user retrieval requests.
 *
 * @since 0.1.8
 */
@RestController
@RequestMapping("/api/1/users")
@RequiredArgsConstructor
public class UserQueryController {

    private final UserRepository userRepository;

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID id) {
        var user =
                userRepository
                        .findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return ResponseEntity.ok(
                new UserResponse(user.getId(), user.getUsername(), user.getEmail()));
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserResponse> getUserByUsername(@PathVariable String username) {
        var user =
                userRepository
                        .findByUsername(username)
                        .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return ResponseEntity.ok(
                new UserResponse(user.getId(), user.getUsername(), user.getEmail()));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        var user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return ResponseEntity.ok(
                new UserResponse(user.getId(), user.getUsername(), user.getEmail()));
    }
}
