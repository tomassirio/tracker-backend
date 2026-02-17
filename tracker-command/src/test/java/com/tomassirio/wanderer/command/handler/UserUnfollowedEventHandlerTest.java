package com.tomassirio.wanderer.command.handler;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tomassirio.wanderer.command.event.UserUnfollowedEvent;
import com.tomassirio.wanderer.command.repository.UserFollowRepository;
import com.tomassirio.wanderer.commons.domain.UserFollow;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserUnfollowedEventHandlerTest {

    @Mock private UserFollowRepository userFollowRepository;

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

        when(userFollowRepository.findByFollowerIdAndFollowedId(followerId, followedId))
                .thenReturn(Optional.of(userFollow));

        // When
        handler.handle(event);

        // Then
        verify(userFollowRepository).delete(userFollow);
    }
}
