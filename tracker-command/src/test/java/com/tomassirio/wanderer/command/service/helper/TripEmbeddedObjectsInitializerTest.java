package com.tomassirio.wanderer.command.service.helper;

import static org.assertj.core.api.Assertions.assertThat;

import com.tomassirio.wanderer.commons.domain.Trip;
import com.tomassirio.wanderer.commons.domain.TripDetails;
import com.tomassirio.wanderer.commons.domain.TripSettings;
import com.tomassirio.wanderer.commons.domain.TripStatus;
import com.tomassirio.wanderer.commons.domain.TripVisibility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TripEmbeddedObjectsInitializerTest {

    private TripEmbeddedObjectsInitializer initializer;

    @BeforeEach
    void setUp() {
        initializer = new TripEmbeddedObjectsInitializer();
    }

    @Test
    void createTripSettings_whenCalledWithPublicVisibility_shouldCreateSettingsWithCreatedStatus() {
        // When
        TripSettings settings = initializer.createTripSettings(TripVisibility.PUBLIC);

        // Then
        assertThat(settings).isNotNull();
        assertThat(settings.getTripStatus()).isEqualTo(TripStatus.CREATED);
        assertThat(settings.getVisibility()).isEqualTo(TripVisibility.PUBLIC);
        assertThat(settings.getUpdateRefresh()).isNull();
    }

    @Test
    void
            createTripSettings_whenCalledWithPrivateVisibility_shouldCreateSettingsWithCreatedStatus() {
        // When
        TripSettings settings = initializer.createTripSettings(TripVisibility.PRIVATE);

        // Then
        assertThat(settings).isNotNull();
        assertThat(settings.getTripStatus()).isEqualTo(TripStatus.CREATED);
        assertThat(settings.getVisibility()).isEqualTo(TripVisibility.PRIVATE);
        assertThat(settings.getUpdateRefresh()).isNull();
    }

    @Test
    void
            createTripSettings_whenCalledWithProtectedVisibility_shouldCreateSettingsWithCreatedStatus() {
        // When
        TripSettings settings = initializer.createTripSettings(TripVisibility.PROTECTED);

        // Then
        assertThat(settings).isNotNull();
        assertThat(settings.getTripStatus()).isEqualTo(TripStatus.CREATED);
        assertThat(settings.getVisibility()).isEqualTo(TripVisibility.PROTECTED);
        assertThat(settings.getUpdateRefresh()).isNull();
    }

    // Tests for createTripDetails()

    @Test
    void createTripDetails_shouldCreateDetailsWithNullFields() {
        // When
        TripDetails details = initializer.createTripDetails();

        // Then
        assertThat(details).isNotNull();
        assertThat(details.getStartTimestamp()).isNull();
        assertThat(details.getEndTimestamp()).isNull();
        assertThat(details.getStartLocation()).isNull();
        assertThat(details.getEndLocation()).isNull();
    }

    // Tests for ensureTripSettings(Trip, TripVisibility)

    @Test
    void ensureTripSettings_whenSettingsIsNull_shouldInitializeWithProvidedVisibility() {
        // Given
        Trip trip = Trip.builder().tripSettings(null).build();

        // When
        initializer.ensureTripSettings(trip, TripVisibility.PUBLIC);

        // Then
        assertThat(trip.getTripSettings()).isNotNull();
        assertThat(trip.getTripSettings().getTripStatus()).isEqualTo(TripStatus.CREATED);
        assertThat(trip.getTripSettings().getVisibility()).isEqualTo(TripVisibility.PUBLIC);
        assertThat(trip.getTripSettings().getUpdateRefresh()).isNull();
    }

    @Test
    void ensureTripSettings_whenSettingsIsNull_shouldInitializeWithPrivateVisibility() {
        // Given
        Trip trip = Trip.builder().tripSettings(null).build();

        // When
        initializer.ensureTripSettings(trip, TripVisibility.PRIVATE);

        // Then
        assertThat(trip.getTripSettings()).isNotNull();
        assertThat(trip.getTripSettings().getVisibility()).isEqualTo(TripVisibility.PRIVATE);
    }

    @Test
    void ensureTripSettings_whenSettingsAlreadyExists_shouldNotModify() {
        // Given
        TripSettings existingSettings =
                TripSettings.builder()
                        .tripStatus(TripStatus.IN_PROGRESS)
                        .visibility(TripVisibility.PRIVATE)
                        .updateRefresh(60)
                        .build();
        Trip trip = Trip.builder().tripSettings(existingSettings).build();

        // When
        initializer.ensureTripSettings(trip, TripVisibility.PUBLIC);

        // Then
        assertThat(trip.getTripSettings()).isEqualTo(existingSettings);
        assertThat(trip.getTripSettings().getTripStatus()).isEqualTo(TripStatus.IN_PROGRESS);
        assertThat(trip.getTripSettings().getVisibility()).isEqualTo(TripVisibility.PRIVATE);
        assertThat(trip.getTripSettings().getUpdateRefresh()).isEqualTo(60);
    }

    // Tests for ensureTripSettingsAndGetPreviousStatus(Trip, TripStatus)

    @Test
    void ensureTripSettingsAndGetPreviousStatus_whenSettingsIsNull_shouldReturnCreatedStatus() {
        // Given
        Trip trip = Trip.builder().tripSettings(null).build();

        // When
        TripStatus previousStatus =
                initializer.ensureTripSettingsAndGetPreviousStatus(trip, TripStatus.IN_PROGRESS);

        // Then
        assertThat(previousStatus).isEqualTo(TripStatus.CREATED);
        assertThat(trip.getTripSettings()).isNotNull();
        assertThat(trip.getTripSettings().getTripStatus()).isEqualTo(TripStatus.IN_PROGRESS);
        assertThat(trip.getTripSettings().getVisibility()).isEqualTo(TripVisibility.PUBLIC);
    }

    @Test
    void
            ensureTripSettingsAndGetPreviousStatus_whenSettingsExists_shouldReturnActualPreviousStatus() {
        // Given
        TripSettings existingSettings =
                TripSettings.builder()
                        .tripStatus(TripStatus.IN_PROGRESS)
                        .visibility(TripVisibility.PRIVATE)
                        .build();
        Trip trip = Trip.builder().tripSettings(existingSettings).build();

        // When
        TripStatus previousStatus =
                initializer.ensureTripSettingsAndGetPreviousStatus(trip, TripStatus.FINISHED);

        // Then
        assertThat(previousStatus).isEqualTo(TripStatus.IN_PROGRESS);
        assertThat(trip.getTripSettings()).isEqualTo(existingSettings);
    }

    @Test
    void
            ensureTripSettingsAndGetPreviousStatus_whenSettingsExistsWithPausedStatus_shouldReturnPaused() {
        // Given
        TripSettings existingSettings =
                TripSettings.builder()
                        .tripStatus(TripStatus.PAUSED)
                        .visibility(TripVisibility.PUBLIC)
                        .build();
        Trip trip = Trip.builder().tripSettings(existingSettings).build();

        // When
        TripStatus previousStatus =
                initializer.ensureTripSettingsAndGetPreviousStatus(trip, TripStatus.IN_PROGRESS);

        // Then
        assertThat(previousStatus).isEqualTo(TripStatus.PAUSED);
    }

    @Test
    void
            ensureTripSettingsAndGetPreviousStatus_whenSettingsIsNull_shouldInitializeWithProvidedStatus() {
        // Given
        Trip trip = Trip.builder().tripSettings(null).build();

        // When
        TripStatus previousStatus =
                initializer.ensureTripSettingsAndGetPreviousStatus(trip, TripStatus.FINISHED);

        // Then
        assertThat(previousStatus).isEqualTo(TripStatus.CREATED);
        assertThat(trip.getTripSettings()).isNotNull();
        assertThat(trip.getTripSettings().getTripStatus()).isEqualTo(TripStatus.FINISHED);
    }

    @Test
    void
            ensureTripSettingsAndGetPreviousStatus_whenSettingsIsNullWithCreatedStatus_shouldReturnCreated() {
        // Given
        Trip trip = Trip.builder().tripSettings(null).build();

        // When
        TripStatus previousStatus =
                initializer.ensureTripSettingsAndGetPreviousStatus(trip, TripStatus.CREATED);

        // Then
        assertThat(previousStatus).isEqualTo(TripStatus.CREATED);
        assertThat(trip.getTripSettings()).isNotNull();
        assertThat(trip.getTripSettings().getTripStatus()).isEqualTo(TripStatus.CREATED);
    }

    // Tests for ensureTripDetails(Trip)

    @Test
    void ensureTripDetails_whenDetailsIsNull_shouldInitializeWithDefaultValues() {
        // Given
        Trip trip = Trip.builder().tripDetails(null).build();

        // When
        initializer.ensureTripDetails(trip);

        // Then
        assertThat(trip.getTripDetails()).isNotNull();
        assertThat(trip.getTripDetails().getStartTimestamp()).isNull();
        assertThat(trip.getTripDetails().getEndTimestamp()).isNull();
        assertThat(trip.getTripDetails().getStartLocation()).isNull();
        assertThat(trip.getTripDetails().getEndLocation()).isNull();
    }

    @Test
    void ensureTripDetails_whenDetailsAlreadyExists_shouldNotModify() {
        // Given
        TripDetails existingDetails =
                TripDetails.builder()
                        .startTimestamp(java.time.Instant.now())
                        .endTimestamp(java.time.Instant.now())
                        .build();
        Trip trip = Trip.builder().tripDetails(existingDetails).build();

        // When
        initializer.ensureTripDetails(trip);

        // Then
        assertThat(trip.getTripDetails()).isEqualTo(existingDetails);
        assertThat(trip.getTripDetails().getStartTimestamp()).isNotNull();
        assertThat(trip.getTripDetails().getEndTimestamp()).isNotNull();
    }

    @Test
    void ensureTripDetails_whenDetailsExistsWithPartialData_shouldNotModify() {
        // Given
        TripDetails existingDetails =
                TripDetails.builder()
                        .startTimestamp(java.time.Instant.now())
                        .endTimestamp(null)
                        .build();
        Trip trip = Trip.builder().tripDetails(existingDetails).build();

        // When
        initializer.ensureTripDetails(trip);

        // Then
        assertThat(trip.getTripDetails()).isEqualTo(existingDetails);
        assertThat(trip.getTripDetails().getStartTimestamp()).isNotNull();
        assertThat(trip.getTripDetails().getEndTimestamp()).isNull();
    }

    // Integration tests

    @Test
    void ensureBothSettingsAndDetails_shouldInitializeBothWhenNull() {
        // Given
        Trip trip = Trip.builder().tripSettings(null).tripDetails(null).build();

        // When
        initializer.ensureTripSettings(trip, TripVisibility.PUBLIC);
        initializer.ensureTripDetails(trip);

        // Then
        assertThat(trip.getTripSettings()).isNotNull();
        assertThat(trip.getTripSettings().getTripStatus()).isEqualTo(TripStatus.CREATED);
        assertThat(trip.getTripSettings().getVisibility()).isEqualTo(TripVisibility.PUBLIC);

        assertThat(trip.getTripDetails()).isNotNull();
        assertThat(trip.getTripDetails().getStartTimestamp()).isNull();
        assertThat(trip.getTripDetails().getEndTimestamp()).isNull();
    }

    @Test
    void ensureSettingsAndGetPreviousStatus_thenEnsureDetails_shouldWorkTogether() {
        // Given
        Trip trip = Trip.builder().tripSettings(null).tripDetails(null).build();

        // When
        TripStatus previousStatus =
                initializer.ensureTripSettingsAndGetPreviousStatus(trip, TripStatus.IN_PROGRESS);
        initializer.ensureTripDetails(trip);

        // Then
        assertThat(previousStatus).isEqualTo(TripStatus.CREATED);
        assertThat(trip.getTripSettings()).isNotNull();
        assertThat(trip.getTripDetails()).isNotNull();
        assertThat(trip.getTripSettings().getTripStatus()).isEqualTo(TripStatus.IN_PROGRESS);
    }
}
