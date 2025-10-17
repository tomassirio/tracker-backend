package com.tomassirio.wanderer.query.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.tomassirio.wanderer.commons.domain.Friendship;
import com.tomassirio.wanderer.commons.dto.FriendshipResponse;
import com.tomassirio.wanderer.query.repository.FriendshipRepository;
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
class FriendshipQueryServiceImplTest {

    @Mock private FriendshipRepository friendshipRepository;

    @InjectMocks private FriendshipQueryServiceImpl friendshipQueryService;

    private UUID userId;
    private UUID friendId;
    private Friendship friendship;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        friendId = UUID.randomUUID();

        friendship =
                Friendship.builder()
                        .id(UUID.randomUUID())
                        .userId(userId)
                        .friendId(friendId)
                        .createdAt(Instant.now())
                        .build();
    }

    @Test
    void getFriends_Success() {
        when(friendshipRepository.findByUserId(userId)).thenReturn(List.of(friendship));

        List<FriendshipResponse> responses = friendshipQueryService.getFriends(userId);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(userId, responses.get(0).userId());
        assertEquals(friendId, responses.get(0).friendId());
    }

    @Test
    void getFriends_EmptyList() {
        when(friendshipRepository.findByUserId(userId)).thenReturn(List.of());

        List<FriendshipResponse> responses = friendshipQueryService.getFriends(userId);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }
}
