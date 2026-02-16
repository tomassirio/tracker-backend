package com.tomassirio.wanderer.command.handler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tomassirio.wanderer.command.event.FriendshipRemovedEvent;
import com.tomassirio.wanderer.commons.domain.Friendship;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.Instant;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FriendshipRemovedEventHandlerTest {

    @Mock private EntityManager entityManager;
    @Mock private TypedQuery<Friendship> friendshipQuery;

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

        when(entityManager.createQuery(anyString(), eq(Friendship.class)))
                .thenReturn(friendshipQuery);
        when(friendshipQuery.setParameter(anyString(), any())).thenReturn(friendshipQuery);
        when(friendshipQuery.getResultStream())
                .thenReturn(Stream.of(friendship1), Stream.of(friendship2));

        // When
        handler.handle(event);

        // Then
        verify(entityManager, times(2)).remove(any(Friendship.class));
    }

    @Test
    void handle_whenNoFriendshipExists_shouldNotDelete() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID friendId = UUID.randomUUID();

        FriendshipRemovedEvent event =
                FriendshipRemovedEvent.builder().userId(userId).friendId(friendId).build();

        when(entityManager.createQuery(anyString(), eq(Friendship.class)))
                .thenReturn(friendshipQuery);
        when(friendshipQuery.setParameter(anyString(), any())).thenReturn(friendshipQuery);
        when(friendshipQuery.getResultStream())
                .thenReturn(Stream.empty())
                .thenReturn(Stream.empty());

        // When
        handler.handle(event);

        // Then
        verify(entityManager, never()).remove(any(Friendship.class));
    }
}
