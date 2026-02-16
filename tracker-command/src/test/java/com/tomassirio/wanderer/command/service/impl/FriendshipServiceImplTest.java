package com.tomassirio.wanderer.command.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.tomassirio.wanderer.command.event.FriendshipCreatedEvent;
import com.tomassirio.wanderer.command.event.FriendshipRemovedEvent;
import com.tomassirio.wanderer.command.repository.FriendshipRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class FriendshipServiceImplTest {

    @Mock private FriendshipRepository friendshipRepository;

    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private FriendshipServiceImpl friendshipService;

    private UUID userId;
    private UUID friendId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        friendId = UUID.randomUUID();
    }

    @Test
    void areFriends_WhenFriendshipExists_ReturnsTrue() {
        // Given
        when(friendshipRepository.existsByUserIdAndFriendId(userId, friendId)).thenReturn(true);

        // When
        boolean result = friendshipService.areFriends(userId, friendId);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void areFriends_WhenReverseFriendshipExists_ReturnsTrue() {
        // Given
        when(friendshipRepository.existsByUserIdAndFriendId(userId, friendId)).thenReturn(false);
        when(friendshipRepository.existsByUserIdAndFriendId(friendId, userId)).thenReturn(true);

        // When
        boolean result = friendshipService.areFriends(userId, friendId);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void areFriends_WhenNoFriendshipExists_ReturnsFalse() {
        // Given
        when(friendshipRepository.existsByUserIdAndFriendId(userId, friendId)).thenReturn(false);
        when(friendshipRepository.existsByUserIdAndFriendId(friendId, userId)).thenReturn(false);

        // When
        boolean result = friendshipService.areFriends(userId, friendId);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void createFriendship_PublishesFriendshipCreatedEvent() {
        // When
        friendshipService.createFriendship(userId, friendId);

        // Then
        ArgumentCaptor<FriendshipCreatedEvent> eventCaptor =
                ArgumentCaptor.forClass(FriendshipCreatedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        FriendshipCreatedEvent event = eventCaptor.getValue();
        assertThat(event.getUserId()).isEqualTo(userId);
        assertThat(event.getFriendId()).isEqualTo(friendId);
    }

    @Test
    void removeFriendship_PublishesFriendshipRemovedEvent() {
        // When
        friendshipService.removeFriendship(userId, friendId);

        // Then
        ArgumentCaptor<FriendshipRemovedEvent> eventCaptor =
                ArgumentCaptor.forClass(FriendshipRemovedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        FriendshipRemovedEvent event = eventCaptor.getValue();
        assertThat(event.getUserId()).isEqualTo(userId);
        assertThat(event.getFriendId()).isEqualTo(friendId);
    }
}
