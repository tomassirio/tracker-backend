package com.tomassirio.wanderer.query.controller;

import com.tomassirio.wanderer.query.dto.UserResponse;
import com.tomassirio.wanderer.query.service.UserQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for user query operations. Handles user retrieval requests.
 *
 * @since 0.1.8
 */
@RestController
@RequestMapping("/api/1/users")
@RequiredArgsConstructor
public class UserQueryController {

    private final UserQueryService userQueryService;

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userQueryService.getUserById(id));
    }

    @GetMapping("/username/{username}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<UserResponse> getUserByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userQueryService.getUserByUsername(username));
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userQueryService.getUserByEmail(email));
    }
}
