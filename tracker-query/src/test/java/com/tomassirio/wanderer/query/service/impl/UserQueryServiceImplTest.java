package com.tomassirio.wanderer.query.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.tomassirio.wanderer.commons.domain.User;
import com.tomassirio.wanderer.query.dto.UserResponse;
import com.tomassirio.wanderer.query.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserQueryServiceImplTest {

    @Mock private UserRepository userRepository;

    @InjectMocks private UserQueryServiceImpl userQueryService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(UUID.randomUUID()).username("testuser").build();
    }

    @Test
    void getUserById_whenUserExists_shouldReturnUserResponse() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        UserResponse result = userQueryService.getUserById(testUser.getId());

        assertEquals(testUser.getId(), result.id());
        assertEquals(testUser.getUsername(), result.username());
    }

    @Test
    void getUserById_whenUserDoesNotExist_shouldThrowEntityNotFoundException() {
        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class, () -> userQueryService.getUserById(nonExistentId));
    }

    @Test
    void getUserByUsername_whenUserExists_shouldReturnUserResponse() {
        when(userRepository.findByUsername(testUser.getUsername()))
                .thenReturn(Optional.of(testUser));

        UserResponse result = userQueryService.getUserByUsername(testUser.getUsername());

        assertEquals(testUser.getId(), result.id());
        assertEquals(testUser.getUsername(), result.username());
    }

    @Test
    void getUserByUsername_whenUserDoesNotExist_shouldThrowEntityNotFoundException() {
        String nonExistentUsername = "nonexistent";
        when(userRepository.findByUsername(nonExistentUsername)).thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> userQueryService.getUserByUsername(nonExistentUsername));
    }
}
