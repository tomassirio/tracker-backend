package com.tomassirio.wanderer.command.handler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tomassirio.wanderer.command.event.FriendshipRemovedEvent;
import com.tomassirio.wanderer.command.repository.FriendshipRepository;
import com.tomassirio.wanderer.commons.domain.Friendship;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FriendshipRemovedEventHandlerTest {

    @Mock private FriendshipRepository friendshipRepository;

    @InjectMocks private FriendshipRemovedEventHandler handler;

    @Test
    void handle_shouldRemoveBothDirectionalFriendships() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID friendId = UUID.randomUUID();

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

        FriendshipRemovedEvent event =
                FriendshipRemovedEvent.builder().userId(userId).friendId(friendId).build();

        when(friendshipRepository.findByUserIdAndFriendId(userId, friendId))
                .thenReturn(Optional.of(friendship1));
        when(friendshipRepository.findByUserIdAndFriendId(friendId, userId))
                .thenReturn(Optional.of(friendship2));

        // When
        handler.handle(event);

        // Then
        verify(friendshipRepository, times(2)).delete(any(Friendship.class));
    }

    @Test
    void handle_whenNoFriendshipExists_shouldNotDelete() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID friendId = UUID.randomUUID();

        FriendshipRemovedEvent event =
                FriendshipRemovedEvent.builder().userId(userId).friendId(friendId).build();

        when(friendshipRepository.findByUserIdAndFriendId(userId, friendId))
                .thenReturn(Optional.empty());
        when(friendshipRepository.findByUserIdAndFriendId(friendId, userId))
                .thenReturn(Optional.empty());

        // When
        handler.handle(event);

        // Then
        verify(friendshipRepository, never()).delete(any(Friendship.class));
    }
}
