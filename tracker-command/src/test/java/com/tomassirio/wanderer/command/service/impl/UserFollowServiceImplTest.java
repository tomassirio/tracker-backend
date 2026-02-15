package com.tomassirio.wanderer.command.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tomassirio.wanderer.command.event.UserFollowedEvent;
import com.tomassirio.wanderer.command.event.UserUnfollowedEvent;
import com.tomassirio.wanderer.command.repository.UserFollowRepository;
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
class UserFollowServiceImplTest {

    @Mock private UserFollowRepository userFollowRepository;

    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private UserFollowServiceImpl userFollowService;

    private UUID followerId;
    private UUID followedId;

    @BeforeEach
    void setUp() {
        followerId = UUID.randomUUID();
        followedId = UUID.randomUUID();
    }

    @Test
    void followUser_Success() {
        // Given
        when(userFollowRepository.existsByFollowerIdAndFollowedId(followerId, followedId))
                .thenReturn(false);

        // When
        UUID response = userFollowService.followUser(followerId, followedId);

        // Then
        assertThat(response).isNotNull();

        ArgumentCaptor<UserFollowedEvent> eventCaptor =
                ArgumentCaptor.forClass(UserFollowedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        UserFollowedEvent event = eventCaptor.getValue();
        assertThat(event.getFollowId()).isEqualTo(response);
        assertThat(event.getFollowerId()).isEqualTo(followerId);
        assertThat(event.getFollowedId()).isEqualTo(followedId);
        assertThat(event.getCreatedAt()).isNotNull();
    }

    @Test
    void followUser_FollowSelf_ThrowsException() {
        // Given
        UUID userId = UUID.randomUUID();

        // When & Then
        assertThatThrownBy(() -> userFollowService.followUser(userId, userId))
                .isInstanceOf(IllegalArgumentException.class);

        verify(eventPublisher, never()).publishEvent(any(UserFollowedEvent.class));
    }

    @Test
    void followUser_AlreadyFollowing_ThrowsException() {
        // Given
        when(userFollowRepository.existsByFollowerIdAndFollowedId(followerId, followedId))
                .thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userFollowService.followUser(followerId, followedId))
                .isInstanceOf(IllegalArgumentException.class);

        verify(eventPublisher, never()).publishEvent(any(UserFollowedEvent.class));
    }

    @Test
    void unfollowUser_Success() {
        // When
        userFollowService.unfollowUser(followerId, followedId);

        // Then
        ArgumentCaptor<UserUnfollowedEvent> eventCaptor =
                ArgumentCaptor.forClass(UserUnfollowedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        UserUnfollowedEvent event = eventCaptor.getValue();
        assertThat(event.getFollowerId()).isEqualTo(followerId);
        assertThat(event.getFollowedId()).isEqualTo(followedId);
    }

    @Test
    void isFollowing_WhenFollowing_ReturnsTrue() {
        // Given
        when(userFollowRepository.existsByFollowerIdAndFollowedId(followerId, followedId))
                .thenReturn(true);

        // When
        boolean result = userFollowService.isFollowing(followerId, followedId);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void isFollowing_WhenNotFollowing_ReturnsFalse() {
        // Given
        when(userFollowRepository.existsByFollowerIdAndFollowedId(followerId, followedId))
                .thenReturn(false);

        // When
        boolean result = userFollowService.isFollowing(followerId, followedId);

        // Then
        assertThat(result).isFalse();
    }
}
