package com.tomassirio.wanderer.query.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.tomassirio.wanderer.commons.domain.UserFollow;
import com.tomassirio.wanderer.commons.dto.UserFollowResponse;
import com.tomassirio.wanderer.query.repository.UserFollowRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserFollowQueryServiceImplTest {

    @Mock private UserFollowRepository userFollowRepository;

    @InjectMocks private UserFollowQueryServiceImpl userFollowQueryService;

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
    void getFollowing_Success() {
        when(userFollowRepository.findByFollowerId(followerId)).thenReturn(List.of(userFollow));

        List<UserFollowResponse> responses = userFollowQueryService.getFollowing(followerId);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(followerId, responses.get(0).followerId());
        assertEquals(followedId, responses.get(0).followedId());
    }

    @Test
    void getFollowing_EmptyList() {
        when(userFollowRepository.findByFollowerId(followerId)).thenReturn(List.of());

        List<UserFollowResponse> responses = userFollowQueryService.getFollowing(followerId);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    void getFollowers_Success() {
        when(userFollowRepository.findByFollowedId(followedId)).thenReturn(List.of(userFollow));

        List<UserFollowResponse> responses = userFollowQueryService.getFollowers(followedId);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(followerId, responses.get(0).followerId());
        assertEquals(followedId, responses.get(0).followedId());
    }

    @Test
    void getFollowers_EmptyList() {
        when(userFollowRepository.findByFollowedId(followedId)).thenReturn(List.of());

        List<UserFollowResponse> responses = userFollowQueryService.getFollowers(followedId);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }
}
