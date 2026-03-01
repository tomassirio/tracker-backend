package com.tomassirio.wanderer.command.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tomassirio.wanderer.command.event.AchievementUnlockedEvent;
import com.tomassirio.wanderer.command.repository.TripRepository;
import com.tomassirio.wanderer.command.repository.TripUpdateRepository;
import com.tomassirio.wanderer.command.repository.UserAchievementRepository;
import com.tomassirio.wanderer.commons.domain.Achievement;
import com.tomassirio.wanderer.commons.domain.AchievementType;
import com.tomassirio.wanderer.commons.domain.GeoLocation;
import com.tomassirio.wanderer.commons.domain.Trip;
import com.tomassirio.wanderer.commons.domain.TripDetails;
import com.tomassirio.wanderer.commons.domain.TripUpdate;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class AchievementCalculationServiceTest {

    @Mock private TripRepository tripRepository;

    @Mock private TripUpdateRepository tripUpdateRepository;

    @Mock private UserAchievementRepository unlockedAchievementRepository;

    @Mock private EntityManager entityManager;

    @Mock private ApplicationEventPublisher eventPublisher;

    @Mock private DistanceCalculationStrategy distanceCalculationStrategy;

    @Mock private TypedQuery<Achievement> query;

    @InjectMocks private AchievementCalculationService service;

    @Test
    void checkAndUnlockAchievements_whenUpdateCountReaches10_shouldUnlockAchievement() {
        // Given
        UUID tripId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Trip trip = Trip.builder().id(tripId).userId(userId).name("Camino").build();

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(tripUpdateRepository.countByTripId(tripId)).thenReturn(10L);
        when(unlockedAchievementRepository.existsByUserIdAndAchievementTypeAndTripId(
                        userId, AchievementType.UPDATES_10, tripId))
                .thenReturn(false);

        Achievement achievement =
                Achievement.builder()
                        .id(UUID.randomUUID())
                        .type(AchievementType.UPDATES_10)
                        .name("Getting Started")
                        .description("Post 10 updates")
                        .thresholdValue(10)
                        .enabled(true)
                        .build();

        when(entityManager.createQuery(anyString(), eq(Achievement.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultStream()).thenReturn(Stream.of(achievement));

        // When
        service.checkAndUnlockAchievements(tripId);

        // Then
        ArgumentCaptor<AchievementUnlockedEvent> captor =
                ArgumentCaptor.forClass(AchievementUnlockedEvent.class);
        verify(eventPublisher, times(1)).publishEvent(captor.capture());

        AchievementUnlockedEvent event = captor.getValue();
        verify(eventPublisher).publishEvent(event);
    }

    @Test
    void checkAndUnlockAchievements_whenAlreadyUnlocked_shouldNotPublishEvent() {
        // Given
        UUID tripId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Trip trip = Trip.builder().id(tripId).userId(userId).name("Camino").build();

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(tripUpdateRepository.countByTripId(tripId)).thenReturn(10L);
        when(unlockedAchievementRepository.existsByUserIdAndAchievementTypeAndTripId(
                        userId, AchievementType.UPDATES_10, tripId))
                .thenReturn(true);

        // When
        service.checkAndUnlockAchievements(tripId);

        // Then
        verify(eventPublisher, never()).publishEvent(any(AchievementUnlockedEvent.class));
    }

    @Test
    void checkAndUnlockAchievements_whenDistanceReaches100km_shouldUnlockAchievement() {
        // Given
        UUID tripId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Trip trip = Trip.builder().id(tripId).userId(userId).name("Camino").build();

        // Create trip updates that span approximately 100km
        // Using coordinates from Santiago to points along Camino
        List<TripUpdate> updates =
                Arrays.asList(
                        createTripUpdate(tripId, 42.8805, -8.5457), // Santiago de Compostela
                        createTripUpdate(tripId, 42.7, -8.3), // ~25km northeast
                        createTripUpdate(tripId, 42.5, -8.1), // ~50km
                        createTripUpdate(tripId, 42.3, -7.9), // ~75km
                        createTripUpdate(tripId, 42.1, -7.7)); // ~100km

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(tripUpdateRepository.countByTripId(tripId)).thenReturn(5L);
        when(tripUpdateRepository.findByTripIdOrderByTimestampAsc(tripId)).thenReturn(updates);
        when(distanceCalculationStrategy.calculatePathDistance(any()))
                .thenReturn(105.0); // Mock 105km
        when(unlockedAchievementRepository.existsByUserIdAndAchievementTypeAndTripId(
                        userId, AchievementType.DISTANCE_100KM, tripId))
                .thenReturn(false);

        Achievement achievement =
                Achievement.builder()
                        .id(UUID.randomUUID())
                        .type(AchievementType.DISTANCE_100KM)
                        .name("First Century")
                        .description("Walk 100km")
                        .thresholdValue(100)
                        .enabled(true)
                        .build();

        when(entityManager.createQuery(anyString(), eq(Achievement.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultStream()).thenReturn(Stream.of(achievement));

        // When
        service.checkAndUnlockAchievements(tripId);

        // Then - verify achievement was unlocked
        ArgumentCaptor<AchievementUnlockedEvent> captor =
                ArgumentCaptor.forClass(AchievementUnlockedEvent.class);
        verify(eventPublisher, times(1)).publishEvent(captor.capture());
    }

    @Test
    void checkAndUnlockAchievements_whenDurationReaches7Days_shouldUnlockAchievement() {
        // Given
        UUID tripId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Instant startTime = Instant.now().minus(8, ChronoUnit.DAYS);
        TripDetails tripDetails =
                TripDetails.builder().startTimestamp(startTime).endTimestamp(null).build();

        Trip trip =
                Trip.builder()
                        .id(tripId)
                        .userId(userId)
                        .name("Camino")
                        .tripDetails(tripDetails)
                        .build();

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(tripUpdateRepository.countByTripId(tripId)).thenReturn(2L);
        when(tripUpdateRepository.findByTripIdOrderByTimestampAsc(tripId)).thenReturn(List.of());
        when(unlockedAchievementRepository.existsByUserIdAndAchievementTypeAndTripId(
                        userId, AchievementType.DURATION_7_DAYS, tripId))
                .thenReturn(false);

        Achievement achievement =
                Achievement.builder()
                        .id(UUID.randomUUID())
                        .type(AchievementType.DURATION_7_DAYS)
                        .name("Week Warrior")
                        .description("Trip lasting 7 days")
                        .thresholdValue(7)
                        .enabled(true)
                        .build();

        when(entityManager.createQuery(anyString(), eq(Achievement.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultStream()).thenReturn(Stream.of(achievement));

        // When
        service.checkAndUnlockAchievements(tripId);

        // Then
        verify(eventPublisher, times(1)).publishEvent(any(AchievementUnlockedEvent.class));
    }

    private TripUpdate createTripUpdate(UUID tripId, double lat, double lon) {
        Trip trip = Trip.builder().id(tripId).name("Test").build();
        return TripUpdate.builder()
                .id(UUID.randomUUID())
                .trip(trip)
                .location(GeoLocation.builder().lat(lat).lon(lon).build())
                .timestamp(Instant.now())
                .build();
    }
}
