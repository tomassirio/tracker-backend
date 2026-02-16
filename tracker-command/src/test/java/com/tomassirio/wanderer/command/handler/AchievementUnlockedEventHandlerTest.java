package com.tomassirio.wanderer.command.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tomassirio.wanderer.command.event.AchievementUnlockedEvent;
import com.tomassirio.wanderer.commons.domain.Achievement;
import com.tomassirio.wanderer.commons.domain.AchievementType;
import com.tomassirio.wanderer.commons.domain.Trip;
import com.tomassirio.wanderer.commons.domain.User;
import com.tomassirio.wanderer.commons.domain.UserAchievement;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AchievementUnlockedEventHandlerTest {

    @Mock private EntityManager entityManager;

    @InjectMocks private AchievementUnlockedEventHandler handler;

    @Test
    void handle_shouldPersistUserAchievement() {
        // Given
        UUID userAchievementId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID achievementId = UUID.randomUUID();
        UUID tripId = UUID.randomUUID();
        Instant unlockedAt = Instant.now();

        AchievementUnlockedEvent event =
                AchievementUnlockedEvent.builder()
                        .userAchievementId(userAchievementId)
                        .userId(userId)
                        .achievementId(achievementId)
                        .tripId(tripId)
                        .achievementType(AchievementType.DISTANCE_100KM)
                        .achievementName("First Century")
                        .valueAchieved(105.5)
                        .unlockedAt(unlockedAt)
                        .build();

        User user = User.builder().id(userId).username("testuser").build();
        Achievement achievement =
                Achievement.builder()
                        .id(achievementId)
                        .type(AchievementType.DISTANCE_100KM)
                        .name("First Century")
                        .description("Walk 100km")
                        .thresholdValue(100)
                        .enabled(true)
                        .build();
        Trip trip = Trip.builder().id(tripId).name("Camino").userId(userId).build();

        when(entityManager.getReference(User.class, userId)).thenReturn(user);
        when(entityManager.getReference(Achievement.class, achievementId)).thenReturn(achievement);
        when(entityManager.getReference(Trip.class, tripId)).thenReturn(trip);

        // When
        handler.handle(event);

        // Then
        ArgumentCaptor<UserAchievement> captor = ArgumentCaptor.forClass(UserAchievement.class);
        verify(entityManager).persist(captor.capture());

        UserAchievement saved = captor.getValue();
        assertThat(saved.getId()).isEqualTo(userAchievementId);
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getAchievement()).isEqualTo(achievement);
        assertThat(saved.getTrip()).isEqualTo(trip);
        assertThat(saved.getUnlockedAt()).isEqualTo(unlockedAt);
        assertThat(saved.getValueAchieved()).isEqualTo(105.5);
    }
}
