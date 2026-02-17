package com.tomassirio.wanderer.query.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.tomassirio.wanderer.commons.domain.Achievement;
import com.tomassirio.wanderer.commons.domain.AchievementType;
import com.tomassirio.wanderer.commons.domain.Trip;
import com.tomassirio.wanderer.commons.domain.User;
import com.tomassirio.wanderer.commons.domain.UserAchievement;
import com.tomassirio.wanderer.commons.dto.AchievementDTO;
import com.tomassirio.wanderer.commons.dto.UserAchievementDTO;
import com.tomassirio.wanderer.query.repository.AchievementRepository;
import com.tomassirio.wanderer.query.repository.UserAchievementRepository;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AchievementQueryServiceImplTest {

    @Mock private AchievementRepository achievementRepository;

    @Mock private UserAchievementRepository unlockedAchievementRepository;

    @InjectMocks private AchievementQueryServiceImpl service;

    @Test
    void getAvailableAchievements_shouldReturnAllEnabledAchievements() {
        // Given
        Achievement achievement1 =
                Achievement.builder()
                        .id(UUID.randomUUID())
                        .type(AchievementType.DISTANCE_100KM)
                        .name("First Century")
                        .description("Walk 100km")
                        .thresholdValue(100)
                        .enabled(true)
                        .build();

        Achievement achievement2 =
                Achievement.builder()
                        .id(UUID.randomUUID())
                        .type(AchievementType.UPDATES_10)
                        .name("Getting Started")
                        .description("Post 10 updates")
                        .thresholdValue(10)
                        .enabled(true)
                        .build();

        when(achievementRepository.findByEnabledTrue())
                .thenReturn(Arrays.asList(achievement1, achievement2));

        // When
        List<AchievementDTO> result = service.getAvailableAchievements();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).type()).isEqualTo(AchievementType.DISTANCE_100KM);
        assertThat(result.get(0).name()).isEqualTo("First Century");
        assertThat(result.get(1).type()).isEqualTo(AchievementType.UPDATES_10);
        assertThat(result.get(1).name()).isEqualTo("Getting Started");
    }

    @Test
    void getUserAchievements_shouldReturnUserAchievements() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID tripId = UUID.randomUUID();
        UUID achievementId = UUID.randomUUID();

        User user = User.builder().id(userId).username("testuser").build();
        Trip trip = Trip.builder().id(tripId).name("Camino").userId(userId).build();
        Achievement achievement =
                Achievement.builder()
                        .id(achievementId)
                        .type(AchievementType.DISTANCE_100KM)
                        .name("First Century")
                        .description("Walk 100km")
                        .thresholdValue(100)
                        .enabled(true)
                        .build();

        UserAchievement userAchievement =
                UserAchievement.builder()
                        .id(UUID.randomUUID())
                        .user(user)
                        .achievement(achievement)
                        .trip(trip)
                        .unlockedAt(Instant.now())
                        .valueAchieved(105.5)
                        .build();

        when(unlockedAchievementRepository.findByUserId(userId))
                .thenReturn(List.of(userAchievement));

        // When
        List<UserAchievementDTO> result = service.getUserAchievements(userId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).userId()).isEqualTo(userId.toString());
        assertThat(result.get(0).tripId()).isEqualTo(tripId.toString());
        assertThat(result.get(0).achievement().type()).isEqualTo(AchievementType.DISTANCE_100KM);
        assertThat(result.get(0).valueAchieved()).isEqualTo(105.5);
    }

    @Test
    void getUserAchievementsByTrip_shouldReturnTripAchievements() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID tripId = UUID.randomUUID();

        User user = User.builder().id(userId).username("testuser").build();
        Trip trip = Trip.builder().id(tripId).name("Camino").userId(userId).build();
        Achievement achievement =
                Achievement.builder()
                        .id(UUID.randomUUID())
                        .type(AchievementType.DISTANCE_100KM)
                        .name("First Century")
                        .description("Walk 100km")
                        .thresholdValue(100)
                        .enabled(true)
                        .build();

        UserAchievement userAchievement =
                UserAchievement.builder()
                        .id(UUID.randomUUID())
                        .user(user)
                        .achievement(achievement)
                        .trip(trip)
                        .unlockedAt(Instant.now())
                        .valueAchieved(105.5)
                        .build();

        when(unlockedAchievementRepository.findByUserIdAndTripId(userId, tripId))
                .thenReturn(List.of(userAchievement));

        // When
        List<UserAchievementDTO> result = service.getUserAchievementsByTrip(userId, tripId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).userId()).isEqualTo(userId.toString());
        assertThat(result.get(0).tripId()).isEqualTo(tripId.toString());
    }
}
