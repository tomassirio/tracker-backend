package com.tomassirio.wanderer.command.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tomassirio.wanderer.command.event.AchievementUnlockedEvent;
import com.tomassirio.wanderer.command.repository.AchievementRepository;
import com.tomassirio.wanderer.command.repository.TripRepository;
import com.tomassirio.wanderer.command.repository.UserAchievementRepository;
import com.tomassirio.wanderer.command.service.impl.AchievementCalculationService;
import com.tomassirio.wanderer.commons.domain.Achievement;
import com.tomassirio.wanderer.commons.domain.AchievementType;
import com.tomassirio.wanderer.commons.domain.Trip;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class AchievementCalculationServiceTest {

    @Mock private TripRepository tripRepository;

    @Mock private UserAchievementRepository userAchievementRepository;

    @Mock private AchievementRepository achievementRepository;

    @Mock private ApplicationEventPublisher eventPublisher;

    @Mock private TripAchievementChecker tripChecker;

    @Mock private SocialAchievementChecker socialChecker;

    private AchievementCalculationService service;

    private void initService() {
        service =
                new AchievementCalculationService(
                        tripRepository,
                        userAchievementRepository,
                        achievementRepository,
                        eventPublisher,
                        List.of(tripChecker),
                        List.of(socialChecker));
    }

    @Test
    void checkAndUnlockAchievements_whenMetricMeetsThreshold_shouldUnlockAchievement() {
        // Given
        initService();
        UUID tripId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Trip trip = Trip.builder().id(tripId).userId(userId).name("Camino").build();

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(tripChecker.getApplicableTypes()).thenReturn(List.of(AchievementType.UPDATES_10));
        when(tripChecker.computeMetric(trip)).thenReturn(15.0);
        when(userAchievementRepository.existsByUserIdAndAchievementTypeAndOptionalTripId(
                        userId, AchievementType.UPDATES_10, tripId))
                .thenReturn(false);

        Achievement achievement = buildAchievement(AchievementType.UPDATES_10, "Getting Started");
        when(achievementRepository.findByTypeAndEnabledTrue(AchievementType.UPDATES_10))
                .thenReturn(Optional.of(achievement));

        // When
        service.checkAndUnlockAchievements(tripId);

        // Then
        ArgumentCaptor<AchievementUnlockedEvent> captor =
                ArgumentCaptor.forClass(AchievementUnlockedEvent.class);
        verify(eventPublisher, times(1)).publishEvent(captor.capture());
        verify(tripChecker).computeMetric(trip);
    }

    @Test
    void checkAndUnlockAchievements_whenAlreadyUnlocked_shouldNotPublishEvent() {
        // Given
        initService();
        UUID tripId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Trip trip = Trip.builder().id(tripId).userId(userId).name("Camino").build();

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(tripChecker.getApplicableTypes()).thenReturn(List.of(AchievementType.UPDATES_10));
        when(tripChecker.computeMetric(trip)).thenReturn(15.0);
        when(userAchievementRepository.existsByUserIdAndAchievementTypeAndOptionalTripId(
                        userId, AchievementType.UPDATES_10, tripId))
                .thenReturn(true);

        // When
        service.checkAndUnlockAchievements(tripId);

        // Then
        verify(eventPublisher, never()).publishEvent(any(AchievementUnlockedEvent.class));
    }

    @Test
    void checkAndUnlockAchievements_whenMetricBelowThreshold_shouldNotUnlock() {
        // Given
        initService();
        UUID tripId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Trip trip = Trip.builder().id(tripId).userId(userId).name("Camino").build();

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(tripChecker.getApplicableTypes()).thenReturn(List.of(AchievementType.UPDATES_10));
        when(tripChecker.computeMetric(trip)).thenReturn(5.0);

        // When
        service.checkAndUnlockAchievements(tripId);

        // Then
        verify(eventPublisher, never()).publishEvent(any(AchievementUnlockedEvent.class));
    }

    @Test
    void checkAndUnlockAchievements_whenMultipleCheckers_shouldEvaluateAll() {
        // Given
        TripAchievementChecker secondChecker =
                new TripAchievementChecker() {
                    @Override
                    public List<AchievementType> getApplicableTypes() {
                        return List.of(AchievementType.DISTANCE_100KM);
                    }

                    @Override
                    public double computeMetric(Trip trip) {
                        return 150.0;
                    }
                };

        service =
                new AchievementCalculationService(
                        tripRepository,
                        userAchievementRepository,
                        achievementRepository,
                        eventPublisher,
                        List.of(tripChecker, secondChecker),
                        List.of(socialChecker));

        UUID tripId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Trip trip = Trip.builder().id(tripId).userId(userId).name("Camino").build();

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(tripChecker.getApplicableTypes()).thenReturn(List.of(AchievementType.UPDATES_10));
        when(tripChecker.computeMetric(trip)).thenReturn(15.0);
        when(userAchievementRepository.existsByUserIdAndAchievementTypeAndOptionalTripId(
                        any(), any(), any()))
                .thenReturn(false);

        Achievement achievement = buildAchievement(AchievementType.UPDATES_10, "Test");
        when(achievementRepository.findByTypeAndEnabledTrue(any()))
                .thenReturn(Optional.of(achievement));

        // When
        service.checkAndUnlockAchievements(tripId);

        // Then — both checkers evaluated, both produced unlockable achievements
        verify(eventPublisher, times(2)).publishEvent(any(AchievementUnlockedEvent.class));
    }

    @Test
    void checkAndUnlockSocialAchievements_whenMetricMeetsThreshold_shouldUnlock() {
        // Given
        initService();
        UUID userId = UUID.randomUUID();

        when(socialChecker.getApplicableTypes()).thenReturn(List.of(AchievementType.FOLLOWERS_10));
        when(socialChecker.computeMetric(userId)).thenReturn(12.0);
        when(userAchievementRepository.existsByUserIdAndAchievementTypeAndOptionalTripId(
                        userId, AchievementType.FOLLOWERS_10, null))
                .thenReturn(false);

        Achievement achievement = buildAchievement(AchievementType.FOLLOWERS_10, "Popular Walker");
        when(achievementRepository.findByTypeAndEnabledTrue(AchievementType.FOLLOWERS_10))
                .thenReturn(Optional.of(achievement));

        // When
        service.checkAndUnlockSocialAchievements(userId);

        // Then
        verify(eventPublisher, times(1)).publishEvent(any(AchievementUnlockedEvent.class));
        verify(socialChecker).computeMetric(userId);
    }

    @Test
    void checkAndUnlockSocialAchievements_whenAlreadyUnlocked_shouldNotPublishEvent() {
        // Given
        initService();
        UUID userId = UUID.randomUUID();

        when(socialChecker.getApplicableTypes()).thenReturn(List.of(AchievementType.FOLLOWERS_10));
        when(socialChecker.computeMetric(userId)).thenReturn(12.0);
        when(userAchievementRepository.existsByUserIdAndAchievementTypeAndOptionalTripId(
                        userId, AchievementType.FOLLOWERS_10, null))
                .thenReturn(true);

        // When
        service.checkAndUnlockSocialAchievements(userId);

        // Then
        verify(eventPublisher, never()).publishEvent(any(AchievementUnlockedEvent.class));
    }

    @Test
    void getOrCreateAchievement_whenNotFound_shouldCreateAndSave() {
        // Given
        initService();
        UUID tripId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Trip trip = Trip.builder().id(tripId).userId(userId).name("Camino").build();

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(tripChecker.getApplicableTypes()).thenReturn(List.of(AchievementType.UPDATES_10));
        when(tripChecker.computeMetric(trip)).thenReturn(15.0);
        when(userAchievementRepository.existsByUserIdAndAchievementTypeAndOptionalTripId(
                        userId, AchievementType.UPDATES_10, tripId))
                .thenReturn(false);
        when(achievementRepository.findByTypeAndEnabledTrue(AchievementType.UPDATES_10))
                .thenReturn(Optional.empty());

        Achievement savedAchievement =
                buildAchievement(AchievementType.UPDATES_10, "Getting Started");
        when(achievementRepository.save(any(Achievement.class))).thenReturn(savedAchievement);

        // When
        service.checkAndUnlockAchievements(tripId);

        // Then — achievement was created via repository
        verify(achievementRepository).save(any(Achievement.class));
        verify(eventPublisher, times(1)).publishEvent(any(AchievementUnlockedEvent.class));
    }

    private Achievement buildAchievement(AchievementType type, String name) {
        return Achievement.builder()
                .id(UUID.randomUUID())
                .type(type)
                .name(name)
                .description(type.getDescription())
                .thresholdValue(type.getThreshold())
                .enabled(true)
                .build();
    }
}
