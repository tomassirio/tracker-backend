package com.tomassirio.wanderer.command.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tomassirio.wanderer.command.repository.FriendshipRepository;
import com.tomassirio.wanderer.commons.domain.Friendship;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FriendshipServiceImplTest {

    @Mock private FriendshipRepository friendshipRepository;

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
        when(friendshipRepository.existsByUserIdAndFriendId(userId, friendId)).thenReturn(true);

        boolean result = friendshipService.areFriends(userId, friendId);

        assertTrue(result);
    }

    @Test
    void areFriends_WhenReverseFriendshipExists_ReturnsTrue() {
        when(friendshipRepository.existsByUserIdAndFriendId(userId, friendId)).thenReturn(false);
        when(friendshipRepository.existsByUserIdAndFriendId(friendId, userId)).thenReturn(true);

        boolean result = friendshipService.areFriends(userId, friendId);

        assertTrue(result);
    }

    @Test
    void areFriends_WhenNoFriendshipExists_ReturnsFalse() {
        when(friendshipRepository.existsByUserIdAndFriendId(userId, friendId)).thenReturn(false);
        when(friendshipRepository.existsByUserIdAndFriendId(friendId, userId)).thenReturn(false);

        boolean result = friendshipService.areFriends(userId, friendId);

        assertFalse(result);
    }

    @Test
    void createFriendship_CreatesBothDirectionalFriendships() {
        when(friendshipRepository.existsByUserIdAndFriendId(userId, friendId)).thenReturn(false);
        when(friendshipRepository.existsByUserIdAndFriendId(friendId, userId)).thenReturn(false);

        friendshipService.createFriendship(userId, friendId);

        verify(friendshipRepository, times(2)).save(any(Friendship.class));
    }

    @Test
    void createFriendship_WhenAlreadyExists_DoesNotCreateDuplicate() {
        when(friendshipRepository.existsByUserIdAndFriendId(userId, friendId)).thenReturn(true);
        when(friendshipRepository.existsByUserIdAndFriendId(friendId, userId)).thenReturn(true);

        friendshipService.createFriendship(userId, friendId);

        verify(friendshipRepository, never()).save(any(Friendship.class));
    }

    @Test
    void removeFriendship_RemovesBothDirectionalFriendships() {
        Friendship friendship1 =
                Friendship.builder()
                        .id(UUID.randomUUID())
                        .userId(userId)
                        .friendId(friendId)
                        .createdAt(Instant.now())
                        .build();

        Friendship friendship2 =
                Friendship.builder()
                        .id(UUID.randomUUID())
                        .userId(friendId)
                        .friendId(userId)
                        .createdAt(Instant.now())
                        .build();

        when(friendshipRepository.findByUserIdAndFriendId(userId, friendId))
                .thenReturn(Optional.of(friendship1));
        when(friendshipRepository.findByUserIdAndFriendId(friendId, userId))
                .thenReturn(Optional.of(friendship2));

        friendshipService.removeFriendship(userId, friendId);

        verify(friendshipRepository).delete(friendship1);
        verify(friendshipRepository).delete(friendship2);
    }

    @Test
    void removeFriendship_WhenNoFriendshipExists_DoesNothing() {
        when(friendshipRepository.findByUserIdAndFriendId(userId, friendId))
                .thenReturn(Optional.empty());
        when(friendshipRepository.findByUserIdAndFriendId(friendId, userId))
                .thenReturn(Optional.empty());

        friendshipService.removeFriendship(userId, friendId);

        verify(friendshipRepository, never()).delete(any(Friendship.class));
    }
}
