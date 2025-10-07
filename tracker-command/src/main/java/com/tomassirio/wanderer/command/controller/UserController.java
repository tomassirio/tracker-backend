package com.tomassirio.wanderer.command.controller;

import com.tomassirio.wanderer.command.dto.UserCreationRequest;
import com.tomassirio.wanderer.command.dto.UserResponse;
import com.tomassirio.wanderer.command.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody UserCreationRequest request) {
        log.info("Received request to create user: {}", request.username());
        UserResponse created = userService.createUser(request);
        log.info("Successfully created user with ID: {}", created.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
