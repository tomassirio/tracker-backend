package com.tomassirio.wanderer.command.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.tomassirio.wanderer.command.dto.UserCreationRequest;
import com.tomassirio.wanderer.command.dto.UserResponse;
import com.tomassirio.wanderer.command.repository.UserRepository;
import com.tomassirio.wanderer.commons.domain.User;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;

    @InjectMocks private UserServiceImpl userService;

    @BeforeEach
    void setUp() {}

    @Test
    void createUser_whenValid_shouldReturnUserResponse() {
        UserCreationRequest req = new UserCreationRequest("johndoe", "john@example.com");
        User saved = User.builder().id(UUID.randomUUID()).username("johndoe").build();

        when(userRepository.findByUsername(req.username())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserResponse resp = userService.createUser(req);
        assertEquals(saved.getId(), resp.id());
        assertEquals(saved.getUsername(), resp.username());
    }

    @Test
    void createUser_whenUsernameExists_shouldThrow() {
        UserCreationRequest req = new UserCreationRequest("johndoe", "john@example.com");
        User existing = User.builder().id(UUID.randomUUID()).username("johndoe").build();

        when(userRepository.findByUsername(req.username())).thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class, () -> userService.createUser(req));
    }
}
