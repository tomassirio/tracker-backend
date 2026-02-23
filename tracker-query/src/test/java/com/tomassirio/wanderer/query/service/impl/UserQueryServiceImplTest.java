package com.tomassirio.wanderer.query.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.tomassirio.wanderer.commons.domain.User;
import com.tomassirio.wanderer.query.dto.UserResponse;
import com.tomassirio.wanderer.query.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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

    @Test
    void getAllUsers_shouldReturnPagedUserResponses() {
        // Given
        User user1 = User.builder().id(UUID.randomUUID()).username("alice").build();
        User user2 = User.builder().id(UUID.randomUUID()).username("bob").build();
        List<User> users = List.of(user1, user2);
        Pageable pageable = PageRequest.of(0, 20, Sort.by("username"));
        Page<User> userPage = new PageImpl<>(users, pageable, users.size());

        when(userRepository.findAll(pageable)).thenReturn(userPage);

        // When
        Page<UserResponse> result = userQueryService.getAllUsers(pageable);

        // Then
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(2, result.getContent().size());
        assertEquals("alice", result.getContent().get(0).username());
        assertEquals("bob", result.getContent().get(1).username());
    }

    @Test
    void getAllUsers_withEmptyResult_shouldReturnEmptyPage() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        Page<User> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(userRepository.findAll(pageable)).thenReturn(emptyPage);

        // When
        Page<UserResponse> result = userQueryService.getAllUsers(pageable);

        // Then
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getContent().size());
    }

    @Test
    void getAllUsers_withPagination_shouldReturnCorrectPage() {
        // Given
        User user1 = User.builder().id(UUID.randomUUID()).username("charlie").build();
        Pageable pageable = PageRequest.of(1, 2, Sort.by("username")); // Second page, 2 per page
        Page<User> userPage = new PageImpl<>(List.of(user1), pageable, 5); // 5 total, showing 1

        when(userRepository.findAll(pageable)).thenReturn(userPage);

        // When
        Page<UserResponse> result = userQueryService.getAllUsers(pageable);

        // Then
        assertEquals(5, result.getTotalElements());
        assertEquals(3, result.getTotalPages()); // 5 items / 2 per page = 3 pages
        assertEquals(1, result.getContent().size());
        assertEquals("charlie", result.getContent().get(0).username());
    }
}
