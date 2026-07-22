package com.tomassirio.wanderer.command.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.tomassirio.wanderer.command.repository.TripRepository;
import com.tomassirio.wanderer.command.service.impl.checker.TripCountAchievementChecker;
import com.tomassirio.wanderer.commons.domain.AchievementType;
import com.tomassirio.wanderer.commons.domain.Trip;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TripCountAchievementCheckerTest {

    @Mock private TripRepository tripRepository;

    private TripCountAchievementChecker checker;

    private static final UUID USER_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        checker = new TripCountAchievementChecker(tripRepository);
    }

    @Test
    void getApplicableTypes_shouldReturnFirstTripOnly() {
        List<AchievementType> types = checker.getApplicableTypes();

        assertThat(types).containsExactly(AchievementType.FIRST_TRIP);
    }

    @Test
    void computeMetric_whenUserHasNoTrips_shouldReturnZero() {
        when(tripRepository.findAllByUserId(USER_ID)).thenReturn(List.of());

        double result = checker.computeMetric(USER_ID);

        assertThat(result).isEqualTo(0.0);
    }

    @Test
    void computeMetric_whenUserHasOneTrip_shouldReturnOne() {
        when(tripRepository.findAllByUserId(USER_ID))
                .thenReturn(List.of(Trip.builder().id(UUID.randomUUID()).build()));

        double result = checker.computeMetric(USER_ID);

        assertThat(result).isEqualTo(1.0);
    }

    @Test
    void computeMetric_whenUserHasMultipleTrips_shouldReturnCorrectCount() {
        when(tripRepository.findAllByUserId(USER_ID))
                .thenReturn(
                        List.of(
                                Trip.builder().id(UUID.randomUUID()).build(),
                                Trip.builder().id(UUID.randomUUID()).build(),
                                Trip.builder().id(UUID.randomUUID()).build()));

        double result = checker.computeMetric(USER_ID);

        assertThat(result).isEqualTo(3.0);
    }
}
