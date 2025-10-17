package com.tomassirio.wanderer.command.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tomassirio.wanderer.command.repository.UserFollowRepository;
import com.tomassirio.wanderer.commons.domain.UserFollow;
import com.tomassirio.wanderer.commons.dto.UserFollowResponse;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserFollowServiceImplTest {

    @Mock private UserFollowRepository userFollowRepository;

    @InjectMocks private UserFollowServiceImpl userFollowService;

    private UUID followerId;
    private UUID followedId;
    private UserFollow userFollow;

    @BeforeEach
    void setUp() {
        followerId = UUID.randomUUID();
        followedId = UUID.randomUUID();

        userFollow =
                UserFollow.builder()
                        .id(UUID.randomUUID())
                        .followerId(followerId)
                        .followedId(followedId)
                        .createdAt(Instant.now())
                        .build();
    }

    @Test
    void followUser_Success() {
        when(userFollowRepository.existsByFollowerIdAndFollowedId(followerId, followedId))
                .thenReturn(false);
        when(userFollowRepository.save(any(UserFollow.class))).thenReturn(userFollow);

        UserFollowResponse response = userFollowService.followUser(followerId, followedId);

        assertNotNull(response);
        assertEquals(followerId, response.followerId());
        assertEquals(followedId, response.followedId());

        verify(userFollowRepository).save(any(UserFollow.class));
    }

    @Test
    void followUser_FollowSelf_ThrowsException() {
        UUID userId = UUID.randomUUID();

        assertThrows(
                IllegalArgumentException.class, () -> userFollowService.followUser(userId, userId));

        verify(userFollowRepository, never()).save(any());
    }

    @Test
    void followUser_AlreadyFollowing_ThrowsException() {
        when(userFollowRepository.existsByFollowerIdAndFollowedId(followerId, followedId))
                .thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> userFollowService.followUser(followerId, followedId));

        verify(userFollowRepository, never()).save(any());
    }

    @Test
    void unfollowUser_Success() {
        userFollowService.unfollowUser(followerId, followedId);

        verify(userFollowRepository).deleteByFollowerIdAndFollowedId(followerId, followedId);
    }

    @Test
    void isFollowing_WhenFollowing_ReturnsTrue() {
        when(userFollowRepository.existsByFollowerIdAndFollowedId(followerId, followedId))
                .thenReturn(true);

        boolean result = userFollowService.isFollowing(followerId, followedId);

        assertTrue(result);
    }

    @Test
    void isFollowing_WhenNotFollowing_ReturnsFalse() {
        when(userFollowRepository.existsByFollowerIdAndFollowedId(followerId, followedId))
                .thenReturn(false);

        boolean result = userFollowService.isFollowing(followerId, followedId);

        assertFalse(result);
    }
}
