package com.tomassirio.wanderer.command.handler.persistence;

import static org.mockito.Mockito.verify;

import com.tomassirio.wanderer.command.event.UserUnfollowedEvent;
import com.tomassirio.wanderer.command.repository.UserFollowRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserUnfollowedEventPersistenceHandlerTest {

    @Mock private UserFollowRepository userFollowRepository;

    @InjectMocks private UserUnfollowedEventPersistenceHandler handler;

    @Test
    void handle_shouldDeleteUserFollow() {
        // Given
        UUID followerId = UUID.randomUUID();
        UUID followedId = UUID.randomUUID();

        UserUnfollowedEvent event =
                UserUnfollowedEvent.builder().followerId(followerId).followedId(followedId).build();

        // When
        handler.handle(event);

        // Then
        verify(userFollowRepository).deleteByFollowerIdAndFollowedId(followerId, followedId);
    }
}
