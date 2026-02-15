package com.tomassirio.wanderer.command.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tomassirio.wanderer.command.dto.UserCreationRequest;
import com.tomassirio.wanderer.command.event.UserCreatedEvent;
import com.tomassirio.wanderer.command.repository.UserRepository;
import com.tomassirio.wanderer.commons.domain.User;
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
}
