package com.tomassirio.wanderer.command.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tomassirio.wanderer.command.event.AchievementUnlockedEvent;
import com.tomassirio.wanderer.command.repository.AchievementRepository;
import com.tomassirio.wanderer.command.repository.TripRepository;
import com.tomassirio.wanderer.command.repository.UserAchievementRepository;
import com.tomassirio.wanderer.command.repository.UserRepository;
import com.tomassirio.wanderer.commons.domain.Achievement;
import com.tomassirio.wanderer.commons.domain.AchievementType;
import com.tomassirio.wanderer.commons.domain.Trip;
import com.tomassirio.wanderer.commons.domain.User;
import com.tomassirio.wanderer.commons.domain.UserAchievement;
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

    @Mock private UserRepository userRepository;
    @Mock private AchievementRepository achievementRepository;
    @Mock private TripRepository tripRepository;
    @Mock private UserAchievementRepository userAchievementRepository;

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

        when(userRepository.getReferenceById(userId)).thenReturn(user);
        when(achievementRepository.getReferenceById(achievementId)).thenReturn(achievement);
        when(tripRepository.getReferenceById(tripId)).thenReturn(trip);

        // When
        handler.handle(event);

        // Then
        ArgumentCaptor<UserAchievement> captor = ArgumentCaptor.forClass(UserAchievement.class);
        verify(userAchievementRepository).save(captor.capture());

        UserAchievement saved = captor.getValue();
        assertThat(saved.getId()).isEqualTo(userAchievementId);
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getAchievement()).isEqualTo(achievement);
        assertThat(saved.getTrip()).isEqualTo(trip);
        assertThat(saved.getUnlockedAt()).isEqualTo(unlockedAt);
        assertThat(saved.getValueAchieved()).isEqualTo(105.5);
    }
}
