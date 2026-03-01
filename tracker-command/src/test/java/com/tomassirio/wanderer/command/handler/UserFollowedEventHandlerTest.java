package com.tomassirio.wanderer.command.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.tomassirio.wanderer.command.event.UserFollowedEvent;
import com.tomassirio.wanderer.command.repository.UserFollowRepository;
import com.tomassirio.wanderer.command.service.AchievementService;
import com.tomassirio.wanderer.commons.domain.UserFollow;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserFollowedEventHandlerTest {

    @Mock private UserFollowRepository userFollowRepository;

    @Mock private AchievementService achievementCalculationService;

    @InjectMocks private UserFollowedEventHandler handler;

    @Test
    void handle_shouldPersistUserFollow() {
        // Given
        UUID followId = UUID.randomUUID();
        UUID followerId = UUID.randomUUID();
        UUID followedId = UUID.randomUUID();
        Instant createdAt = Instant.now();

        UserFollowedEvent event =
                UserFollowedEvent.builder()
                        .followId(followId)
                        .followerId(followerId)
                        .followedId(followedId)
                        .createdAt(createdAt)
                        .build();

        // When
        handler.handle(event);

        // Then
        ArgumentCaptor<UserFollow> captor = ArgumentCaptor.forClass(UserFollow.class);
        verify(userFollowRepository).save(captor.capture());

        UserFollow saved = captor.getValue();
        assertThat(saved.getId()).isEqualTo(followId);
        assertThat(saved.getFollowerId()).isEqualTo(followerId);
        assertThat(saved.getFollowedId()).isEqualTo(followedId);
        assertThat(saved.getCreatedAt()).isEqualTo(createdAt);

        // Verify achievement calculation was triggered
        verify(achievementCalculationService).checkAndUnlockSocialAchievements(followedId);
    }
}
