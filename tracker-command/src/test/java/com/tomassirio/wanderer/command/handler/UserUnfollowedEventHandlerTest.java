package com.tomassirio.wanderer.command.handler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tomassirio.wanderer.command.event.UserUnfollowedEvent;
import com.tomassirio.wanderer.commons.domain.UserFollow;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserUnfollowedEventHandlerTest {

    @Mock private EntityManager entityManager;
    @Mock private TypedQuery<UserFollow> userFollowQuery;

    @InjectMocks private UserUnfollowedEventHandler handler;

    @Test
    void handle_shouldDeleteUserFollow() {
        // Given
        UUID followerId = UUID.randomUUID();
        UUID followedId = UUID.randomUUID();
        UserFollow userFollow =
                UserFollow.builder()
                        .id(UUID.randomUUID())
                        .followerId(followerId)
                        .followedId(followedId)
                        .build();

        UserUnfollowedEvent event =
                UserUnfollowedEvent.builder().followerId(followerId).followedId(followedId).build();

        when(entityManager.createQuery(anyString(), eq(UserFollow.class)))
                .thenReturn(userFollowQuery);
        when(userFollowQuery.setParameter(anyString(), any())).thenReturn(userFollowQuery);
        when(userFollowQuery.getResultStream()).thenReturn(Stream.of(userFollow));

        // When
        handler.handle(event);

        // Then
        verify(entityManager).remove(userFollow);
    }
}
