package com.tomassirio.wanderer.command.handler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tomassirio.wanderer.command.event.FriendshipCreatedEvent;
import com.tomassirio.wanderer.command.repository.FriendshipRepository;
import com.tomassirio.wanderer.command.service.AchievementService;
import com.tomassirio.wanderer.commons.domain.Friendship;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FriendshipCreatedEventHandlerTest {

    @Mock private FriendshipRepository friendshipRepository;
    @Mock private AchievementService achievementCalculationService;

    @InjectMocks private FriendshipCreatedEventHandler handler;

    @Test
    void handle_shouldCreateBidirectionalFriendship() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID friendId = UUID.randomUUID();

        FriendshipCreatedEvent event =
                FriendshipCreatedEvent.builder().userId(userId).friendId(friendId).build();

        when(friendshipRepository.existsByUserIdAndFriendId(userId, friendId)).thenReturn(false);
        when(friendshipRepository.existsByUserIdAndFriendId(friendId, userId)).thenReturn(false);

        // When
        handler.handle(event);

        // Then
        verify(friendshipRepository, times(2)).save(any(Friendship.class));
        // Verify achievement calculation was triggered for both users
        verify(achievementCalculationService).checkAndUnlockSocialAchievements(userId);
        verify(achievementCalculationService).checkAndUnlockSocialAchievements(friendId);
    }

    @Test
    void handle_whenFriendshipAlreadyExists_shouldNotCreateDuplicate() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID friendId = UUID.randomUUID();

        FriendshipCreatedEvent event =
                FriendshipCreatedEvent.builder().userId(userId).friendId(friendId).build();

        when(friendshipRepository.existsByUserIdAndFriendId(userId, friendId)).thenReturn(true);
        when(friendshipRepository.existsByUserIdAndFriendId(friendId, userId)).thenReturn(true);

        // When
        handler.handle(event);

        // Then
        verify(friendshipRepository, never()).save(any(Friendship.class));
        // Achievement calculation still triggered even if friendship exists
        verify(achievementCalculationService).checkAndUnlockSocialAchievements(userId);
        verify(achievementCalculationService).checkAndUnlockSocialAchievements(friendId);
    }

    @Test
    void handle_whenOnlyOneDirectionExists_shouldCreateMissingSide() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID friendId = UUID.randomUUID();

        FriendshipCreatedEvent event =
                FriendshipCreatedEvent.builder().userId(userId).friendId(friendId).build();

        when(friendshipRepository.existsByUserIdAndFriendId(userId, friendId)).thenReturn(true);
        when(friendshipRepository.existsByUserIdAndFriendId(friendId, userId)).thenReturn(false);

        // When
        handler.handle(event);

        // Then
        verify(friendshipRepository, times(1)).save(any(Friendship.class));
        // Achievement calculation triggered for both users
        verify(achievementCalculationService).checkAndUnlockSocialAchievements(userId);
        verify(achievementCalculationService).checkAndUnlockSocialAchievements(friendId);
    }
}
