package com.tomassirio.wanderer.command.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tomassirio.wanderer.command.event.DonationLinkUpdatedEvent;
import com.tomassirio.wanderer.command.event.TripPromotedEvent;
import com.tomassirio.wanderer.command.event.TripUnpromotedEvent;
import com.tomassirio.wanderer.command.repository.PromotedTripRepository;
import com.tomassirio.wanderer.command.repository.TripRepository;
import com.tomassirio.wanderer.commons.domain.PromotedTrip;
import com.tomassirio.wanderer.commons.domain.Trip;
import com.tomassirio.wanderer.commons.domain.TripDetails;
import com.tomassirio.wanderer.commons.domain.TripSettings;
import com.tomassirio.wanderer.commons.domain.TripStatus;
import com.tomassirio.wanderer.commons.domain.TripVisibility;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class PromotedTripServiceImplTest {

    private static final UUID USER_ID = UUID.randomUUID();

    @Mock private TripRepository tripRepository;

    @Mock private PromotedTripRepository promotedTripRepository;

    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private PromotedTripServiceImpl promotedTripService;

    @Test
    void promoteTrip_whenTripExists_shouldPublishEventAndReturnId() {
        // Given
        UUID adminId = UUID.randomUUID();
        UUID tripId = UUID.randomUUID();
        String donationLink = "https://example.com/donate";

        Trip existingTrip =
                Trip.builder()
                        .id(tripId)
                        .name("Trip Name")
                        .userId(USER_ID)
                        .tripSettings(
                                TripSettings.builder()
                                        .tripStatus(TripStatus.IN_PROGRESS)
                                        .visibility(TripVisibility.PUBLIC)
                                        .build())
                        .tripDetails(TripDetails.builder().build())
                        .creationTimestamp(Instant.now())
                        .enabled(true)
                        .build();

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(existingTrip));
        when(promotedTripRepository.existsByTripId(tripId)).thenReturn(false);

        // When
        UUID result = promotedTripService.promoteTrip(adminId, tripId, donationLink, false, null);

        // Then
        assertThat(result).isNotNull();
        verify(tripRepository).findById(tripId);
        verify(promotedTripRepository).existsByTripId(tripId);
        verify(eventPublisher).publishEvent(any(TripPromotedEvent.class));
    }

    @Test
    void promoteTrip_whenTripNotFound_shouldThrowEntityNotFoundException() {
        // Given
        UUID adminId = UUID.randomUUID();
        UUID tripId = UUID.randomUUID();
        String donationLink = "https://example.com/donate";

        when(tripRepository.findById(tripId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(
                        () ->
                                promotedTripService.promoteTrip(
                                        adminId, tripId, donationLink, false, null))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Trip not found");

        verify(tripRepository).findById(tripId);
        verify(promotedTripRepository, never()).existsByTripId(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void promoteTrip_whenTripAlreadyPromoted_shouldThrowIllegalStateException() {
        // Given
        UUID adminId = UUID.randomUUID();
        UUID tripId = UUID.randomUUID();
        String donationLink = "https://example.com/donate";

        Trip existingTrip =
                Trip.builder()
                        .id(tripId)
                        .name("Trip Name")
                        .userId(USER_ID)
                        .tripSettings(
                                TripSettings.builder()
                                        .tripStatus(TripStatus.IN_PROGRESS)
                                        .visibility(TripVisibility.PUBLIC)
                                        .build())
                        .tripDetails(TripDetails.builder().build())
                        .creationTimestamp(Instant.now())
                        .enabled(true)
                        .build();

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(existingTrip));
        when(promotedTripRepository.existsByTripId(tripId)).thenReturn(true);

        // When & Then
        assertThatThrownBy(
                        () ->
                                promotedTripService.promoteTrip(
                                        adminId, tripId, donationLink, false, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Trip is already promoted");

        verify(tripRepository).findById(tripId);
        verify(promotedTripRepository).existsByTripId(tripId);
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void promoteTrip_withoutDonationLink_shouldPublishEventAndReturnId() {
        // Given
        UUID adminId = UUID.randomUUID();
        UUID tripId = UUID.randomUUID();

        Trip existingTrip =
                Trip.builder()
                        .id(tripId)
                        .name("Trip Name")
                        .userId(USER_ID)
                        .tripSettings(
                                TripSettings.builder()
                                        .tripStatus(TripStatus.IN_PROGRESS)
                                        .visibility(TripVisibility.PUBLIC)
                                        .build())
                        .tripDetails(TripDetails.builder().build())
                        .creationTimestamp(Instant.now())
                        .enabled(true)
                        .build();

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(existingTrip));
        when(promotedTripRepository.existsByTripId(tripId)).thenReturn(false);

        // When
        UUID result = promotedTripService.promoteTrip(adminId, tripId, null, false, null);

        // Then
        assertThat(result).isNotNull();
        verify(tripRepository).findById(tripId);
        verify(promotedTripRepository).existsByTripId(tripId);
        verify(eventPublisher).publishEvent(any(TripPromotedEvent.class));
    }

    @Test
    void promoteTrip_withPreAnnounced_shouldPublishEventWithCountdownDate() {
        // Given
        UUID adminId = UUID.randomUUID();
        UUID tripId = UUID.randomUUID();
        Instant countdownStartDate = Instant.parse("2025-06-01T08:00:00Z");

        Trip existingTrip =
                Trip.builder()
                        .id(tripId)
                        .name("Trip Name")
                        .userId(USER_ID)
                        .tripSettings(
                                TripSettings.builder()
                                        .tripStatus(TripStatus.IN_PROGRESS)
                                        .visibility(TripVisibility.PUBLIC)
                                        .build())
                        .tripDetails(TripDetails.builder().build())
                        .creationTimestamp(Instant.now())
                        .enabled(true)
                        .build();

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(existingTrip));
        when(promotedTripRepository.existsByTripId(tripId)).thenReturn(false);

        // When
        UUID result =
                promotedTripService.promoteTrip(adminId, tripId, null, true, countdownStartDate);

        // Then
        assertThat(result).isNotNull();
        verify(tripRepository).findById(tripId);
        verify(promotedTripRepository).existsByTripId(tripId);

        ArgumentCaptor<TripPromotedEvent> captor = ArgumentCaptor.forClass(TripPromotedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        TripPromotedEvent publishedEvent = captor.getValue();
        assertThat(publishedEvent.isPreAnnounced()).isTrue();
        assertThat(publishedEvent.getCountdownStartDate()).isEqualTo(countdownStartDate);
    }

    @Test
    void promoteTrip_whenPreAnnouncedWithoutCountdownDate_shouldThrowIllegalArgumentException() {
        // Given
        UUID adminId = UUID.randomUUID();
        UUID tripId = UUID.randomUUID();

        Trip existingTrip =
                Trip.builder()
                        .id(tripId)
                        .name("Trip Name")
                        .userId(USER_ID)
                        .tripSettings(
                                TripSettings.builder()
                                        .tripStatus(TripStatus.IN_PROGRESS)
                                        .visibility(TripVisibility.PUBLIC)
                                        .build())
                        .tripDetails(TripDetails.builder().build())
                        .creationTimestamp(Instant.now())
                        .enabled(true)
                        .build();

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(existingTrip));
        when(promotedTripRepository.existsByTripId(tripId)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> promotedTripService.promoteTrip(adminId, tripId, null, true, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("countdownStartDate is required when isPreAnnounced is true");

        verify(tripRepository).findById(tripId);
        verify(promotedTripRepository).existsByTripId(tripId);
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void unpromoteTrip_whenTripIsPromoted_shouldPublishEvent() {
        // Given
        UUID adminId = UUID.randomUUID();
        UUID tripId = UUID.randomUUID();

        Trip existingTrip =
                Trip.builder()
                        .id(tripId)
                        .name("Trip Name")
                        .userId(USER_ID)
                        .tripSettings(
                                TripSettings.builder()
                                        .tripStatus(TripStatus.IN_PROGRESS)
                                        .visibility(TripVisibility.PUBLIC)
                                        .build())
                        .tripDetails(TripDetails.builder().build())
                        .creationTimestamp(Instant.now())
                        .enabled(true)
                        .build();

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(existingTrip));
        when(promotedTripRepository.existsByTripId(tripId)).thenReturn(true);

        // When
        promotedTripService.unpromoteTrip(adminId, tripId);

        // Then
        verify(tripRepository).findById(tripId);
        verify(promotedTripRepository).existsByTripId(tripId);
        verify(eventPublisher).publishEvent(any(TripUnpromotedEvent.class));
    }

    @Test
    void unpromoteTrip_whenTripNotPromoted_shouldThrowEntityNotFoundException() {
        // Given
        UUID adminId = UUID.randomUUID();
        UUID tripId = UUID.randomUUID();

        Trip existingTrip =
                Trip.builder()
                        .id(tripId)
                        .name("Trip Name")
                        .userId(USER_ID)
                        .tripSettings(
                                TripSettings.builder()
                                        .tripStatus(TripStatus.IN_PROGRESS)
                                        .visibility(TripVisibility.PUBLIC)
                                        .build())
                        .tripDetails(TripDetails.builder().build())
                        .creationTimestamp(Instant.now())
                        .enabled(true)
                        .build();

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(existingTrip));
        when(promotedTripRepository.existsByTripId(tripId)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> promotedTripService.unpromoteTrip(adminId, tripId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Trip is not promoted");

        verify(tripRepository).findById(tripId);
        verify(promotedTripRepository).existsByTripId(tripId);
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void updatePromotedTripDonationLink_whenTripIsPromoted_shouldPublishEvent() {
        // Given
        UUID adminId = UUID.randomUUID();
        UUID tripId = UUID.randomUUID();
        UUID promotedTripId = UUID.randomUUID();
        String newDonationLink = "https://example.com/new-donate";

        Trip existingTrip =
                Trip.builder()
                        .id(tripId)
                        .name("Trip Name")
                        .userId(USER_ID)
                        .tripSettings(
                                TripSettings.builder()
                                        .tripStatus(TripStatus.IN_PROGRESS)
                                        .visibility(TripVisibility.PUBLIC)
                                        .build())
                        .tripDetails(TripDetails.builder().build())
                        .creationTimestamp(Instant.now())
                        .enabled(true)
                        .build();

        PromotedTrip promotedTrip =
                PromotedTrip.builder()
                        .id(promotedTripId)
                        .tripId(tripId)
                        .donationLink("https://example.com/old-donate")
                        .promotedBy(adminId)
                        .promotedAt(Instant.now())
                        .build();

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(existingTrip));
        when(promotedTripRepository.findByTripId(tripId)).thenReturn(Optional.of(promotedTrip));

        // When
        UUID result =
                promotedTripService.updatePromotedTripDonationLink(
                        adminId, tripId, newDonationLink);

        // Then
        assertThat(result).isEqualTo(promotedTripId);
        verify(tripRepository).findById(tripId);
        verify(promotedTripRepository).findByTripId(tripId);
        verify(eventPublisher).publishEvent(any(DonationLinkUpdatedEvent.class));
    }

    @Test
    void updatePromotedTripDonationLink_whenTripNotPromoted_shouldThrowEntityNotFoundException() {
        // Given
        UUID adminId = UUID.randomUUID();
        UUID tripId = UUID.randomUUID();
        String newDonationLink = "https://example.com/new-donate";

        Trip existingTrip =
                Trip.builder()
                        .id(tripId)
                        .name("Trip Name")
                        .userId(USER_ID)
                        .tripSettings(
                                TripSettings.builder()
                                        .tripStatus(TripStatus.IN_PROGRESS)
                                        .visibility(TripVisibility.PUBLIC)
                                        .build())
                        .tripDetails(TripDetails.builder().build())
                        .creationTimestamp(Instant.now())
                        .enabled(true)
                        .build();

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(existingTrip));
        when(promotedTripRepository.findByTripId(tripId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(
                        () ->
                                promotedTripService.updatePromotedTripDonationLink(
                                        adminId, tripId, newDonationLink))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Trip is not promoted");

        verify(tripRepository).findById(tripId);
        verify(promotedTripRepository).findByTripId(tripId);
        verify(eventPublisher, never()).publishEvent(any());
    }
}
