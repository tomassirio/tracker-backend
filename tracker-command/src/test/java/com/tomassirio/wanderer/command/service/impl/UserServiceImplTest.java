package com.tomassirio.wanderer.command.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tomassirio.wanderer.command.client.TrackerAuthClient;
import com.tomassirio.wanderer.command.controller.request.UserCreationRequest;
import com.tomassirio.wanderer.command.event.UserCreatedEvent;
import com.tomassirio.wanderer.command.event.UserDeletedEvent;
import com.tomassirio.wanderer.command.repository.UserRepository;
import com.tomassirio.wanderer.commons.domain.User;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;

    @Mock private ApplicationEventPublisher eventPublisher;

    @Mock private TrackerAuthClient trackerAuthClient;

    @InjectMocks private UserServiceImpl userService;

    @Test
    void createUser_whenValid_shouldPublishEventAndReturnUserId() {
        // Given
        UserCreationRequest req = new UserCreationRequest("johndoe", "john@example.com");
        when(userRepository.findByUsername(req.username())).thenReturn(Optional.empty());

        // When
        UUID result = userService.createUser(req);

        // Then
        assertThat(result).isNotNull();

        ArgumentCaptor<UserCreatedEvent> eventCaptor =
                ArgumentCaptor.forClass(UserCreatedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        UserCreatedEvent event = eventCaptor.getValue();
        assertThat(event.getUserId()).isEqualTo(result);
        assertThat(event.getUsername()).isEqualTo("johndoe");
    }

    @Test
    void createUser_whenUsernameExists_shouldThrowException() {
        // Given
        UserCreationRequest req = new UserCreationRequest("johndoe", "john@example.com");
        User existing = User.builder().id(UUID.randomUUID()).username("johndoe").build();
        when(userRepository.findByUsername(req.username())).thenReturn(Optional.of(existing));

        // When & Then
        assertThatThrownBy(() -> userService.createUser(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username already in use");

        verify(eventPublisher, never()).publishEvent(any(UserCreatedEvent.class));
    }

    @Test
    void deleteUser_whenUserExists_shouldPublishEventAndDeleteCredentials() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).username("johndoe").build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        doNothing().when(trackerAuthClient).deleteCredentials(userId);

        // When
        userService.deleteUser(userId);

        // Then
        ArgumentCaptor<UserDeletedEvent> eventCaptor =
                ArgumentCaptor.forClass(UserDeletedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getUserId()).isEqualTo(userId);
        verify(trackerAuthClient).deleteCredentials(userId);
    }

    @Test
    void deleteUser_whenUserNotFound_shouldThrowException() {
        // Given
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(eventPublisher, never()).publishEvent(any(UserDeletedEvent.class));
    }

    @Test
    void deleteUserData_whenUserExists_shouldPublishEventWithoutDeletingCredentials() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).username("johndoe").build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        userService.deleteUserData(userId);

        // Then
        ArgumentCaptor<UserDeletedEvent> eventCaptor =
                ArgumentCaptor.forClass(UserDeletedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getUserId()).isEqualTo(userId);
        verify(trackerAuthClient, never()).deleteCredentials(any());
    }

    @Test
    void deleteUserData_whenUserNotFound_shouldThrowException() {
        // Given
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.deleteUserData(userId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(eventPublisher, never()).publishEvent(any(UserDeletedEvent.class));
    }
}
