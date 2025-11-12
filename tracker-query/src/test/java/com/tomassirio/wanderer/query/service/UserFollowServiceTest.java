package com.tomassirio.wanderer.query.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tomassirio.wanderer.commons.domain.UserFollow;
import com.tomassirio.wanderer.commons.dto.UserFollowResponse;
import com.tomassirio.wanderer.query.repository.UserFollowRepository;
import com.tomassirio.wanderer.query.service.impl.UserFollowServiceImpl;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserFollowServiceTest {

    @Mock private UserFollowRepository userFollowRepository;

    @InjectMocks private UserFollowServiceImpl userFollowService;

    // ============ GET FOLLOWING TESTS ============

    @Test
    void getFollowing_whenUserHasFollowing_shouldReturnFollowingList() {
        // Given
        UUID followerId = UUID.randomUUID();
        UUID followedId1 = UUID.randomUUID();
        UUID followedId2 = UUID.randomUUID();
        Instant now = Instant.now();

        UserFollow follow1 =
                UserFollow.builder()
                        .id(UUID.randomUUID())
                        .followerId(followerId)
                        .followedId(followedId1)
                        .createdAt(now)
                        .build();

        UserFollow follow2 =
                UserFollow.builder()
                        .id(UUID.randomUUID())
                        .followerId(followerId)
                        .followedId(followedId2)
                        .createdAt(now)
                        .build();

        when(userFollowRepository.findByFollowerId(followerId))
                .thenReturn(List.of(follow1, follow2));

        // When
        List<UserFollowResponse> result = userFollowService.getFollowing(followerId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).followerId()).isEqualTo(followerId);
        assertThat(result.get(0).followedId()).isEqualTo(followedId1);
        assertThat(result.get(1).followerId()).isEqualTo(followerId);
        assertThat(result.get(1).followedId()).isEqualTo(followedId2);

        verify(userFollowRepository).findByFollowerId(followerId);
    }

    @Test
    void getFollowing_whenUserHasNoFollowing_shouldReturnEmptyList() {
        // Given
        UUID followerId = UUID.randomUUID();
        when(userFollowRepository.findByFollowerId(followerId)).thenReturn(Collections.emptyList());

        // When
        List<UserFollowResponse> result = userFollowService.getFollowing(followerId);

        // Then
        assertThat(result).isEmpty();
        verify(userFollowRepository).findByFollowerId(followerId);
    }

    @Test
    void getFollowing_shouldMapAllFieldsCorrectly() {
        // Given
        UUID followerId = UUID.randomUUID();
        UUID followedId = UUID.randomUUID();
        UUID followId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2025-11-12T10:00:00Z");

        UserFollow follow =
                UserFollow.builder()
                        .id(followId)
                        .followerId(followerId)
                        .followedId(followedId)
                        .createdAt(createdAt)
                        .build();

        when(userFollowRepository.findByFollowerId(followerId)).thenReturn(List.of(follow));

        // When
        List<UserFollowResponse> result = userFollowService.getFollowing(followerId);

        // Then
        assertThat(result).hasSize(1);
        UserFollowResponse response = result.get(0);
        assertThat(response.id()).isEqualTo(followId);
        assertThat(response.followerId()).isEqualTo(followerId);
        assertThat(response.followedId()).isEqualTo(followedId);
        assertThat(response.createdAt()).isEqualTo(createdAt);

        verify(userFollowRepository).findByFollowerId(followerId);
    }

    // ============ GET FOLLOWERS TESTS ============

    @Test
    void getFollowers_whenUserHasFollowers_shouldReturnFollowersList() {
        // Given
        UUID followedId = UUID.randomUUID();
        UUID followerId1 = UUID.randomUUID();
        UUID followerId2 = UUID.randomUUID();
        Instant now = Instant.now();

        UserFollow follow1 =
                UserFollow.builder()
                        .id(UUID.randomUUID())
                        .followerId(followerId1)
                        .followedId(followedId)
                        .createdAt(now)
                        .build();

        UserFollow follow2 =
                UserFollow.builder()
                        .id(UUID.randomUUID())
                        .followerId(followerId2)
                        .followedId(followedId)
                        .createdAt(now)
                        .build();

        when(userFollowRepository.findByFollowedId(followedId))
                .thenReturn(List.of(follow1, follow2));

        // When
        List<UserFollowResponse> result = userFollowService.getFollowers(followedId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).followedId()).isEqualTo(followedId);
        assertThat(result.get(0).followerId()).isEqualTo(followerId1);
        assertThat(result.get(1).followedId()).isEqualTo(followedId);
        assertThat(result.get(1).followerId()).isEqualTo(followerId2);

        verify(userFollowRepository).findByFollowedId(followedId);
    }

    @Test
    void getFollowers_whenUserHasNoFollowers_shouldReturnEmptyList() {
        // Given
        UUID followedId = UUID.randomUUID();
        when(userFollowRepository.findByFollowedId(followedId)).thenReturn(Collections.emptyList());

        // When
        List<UserFollowResponse> result = userFollowService.getFollowers(followedId);

        // Then
        assertThat(result).isEmpty();
        verify(userFollowRepository).findByFollowedId(followedId);
    }

    @Test
    void getFollowers_shouldMapAllFieldsCorrectly() {
        // Given
        UUID followedId = UUID.randomUUID();
        UUID followerId = UUID.randomUUID();
        UUID followId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2025-11-12T10:00:00Z");

        UserFollow follow =
                UserFollow.builder()
                        .id(followId)
                        .followerId(followerId)
                        .followedId(followedId)
                        .createdAt(createdAt)
                        .build();

        when(userFollowRepository.findByFollowedId(followedId)).thenReturn(List.of(follow));

        // When
        List<UserFollowResponse> result = userFollowService.getFollowers(followedId);

        // Then
        assertThat(result).hasSize(1);
        UserFollowResponse response = result.get(0);
        assertThat(response.id()).isEqualTo(followId);
        assertThat(response.followerId()).isEqualTo(followerId);
        assertThat(response.followedId()).isEqualTo(followedId);
        assertThat(response.createdAt()).isEqualTo(createdAt);

        verify(userFollowRepository).findByFollowedId(followedId);
    }

    // ============ ADDITIONAL TESTS ============

    @Test
    void getFollowing_withMultipleFollows_shouldReturnAllInOrder() {
        // Given
        UUID followerId = UUID.randomUUID();
        List<UserFollow> follows =
                List.of(
                        UserFollow.builder()
                                .id(UUID.randomUUID())
                                .followerId(followerId)
                                .followedId(UUID.randomUUID())
                                .createdAt(Instant.now())
                                .build(),
                        UserFollow.builder()
                                .id(UUID.randomUUID())
                                .followerId(followerId)
                                .followedId(UUID.randomUUID())
                                .createdAt(Instant.now())
                                .build(),
                        UserFollow.builder()
                                .id(UUID.randomUUID())
                                .followerId(followerId)
                                .followedId(UUID.randomUUID())
                                .createdAt(Instant.now())
                                .build());

        when(userFollowRepository.findByFollowerId(followerId)).thenReturn(follows);

        // When
        List<UserFollowResponse> result = userFollowService.getFollowing(followerId);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).allMatch(response -> response.followerId().equals(followerId));
        verify(userFollowRepository).findByFollowerId(followerId);
    }

    @Test
    void getFollowers_withMultipleFollowers_shouldReturnAllInOrder() {
        // Given
        UUID followedId = UUID.randomUUID();
        List<UserFollow> followers =
                List.of(
                        UserFollow.builder()
                                .id(UUID.randomUUID())
                                .followerId(UUID.randomUUID())
                                .followedId(followedId)
                                .createdAt(Instant.now())
                                .build(),
                        UserFollow.builder()
                                .id(UUID.randomUUID())
                                .followerId(UUID.randomUUID())
                                .followedId(followedId)
                                .createdAt(Instant.now())
                                .build(),
                        UserFollow.builder()
                                .id(UUID.randomUUID())
                                .followerId(UUID.randomUUID())
                                .followedId(followedId)
                                .createdAt(Instant.now())
                                .build());

        when(userFollowRepository.findByFollowedId(followedId)).thenReturn(followers);

        // When
        List<UserFollowResponse> result = userFollowService.getFollowers(followedId);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).allMatch(response -> response.followedId().equals(followedId));
        verify(userFollowRepository).findByFollowedId(followedId);
    }
}
