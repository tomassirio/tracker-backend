package com.tomassirio.wanderer.command.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.tomassirio.wanderer.command.event.TripPromotedEvent;
import com.tomassirio.wanderer.command.repository.PromotedTripRepository;
import com.tomassirio.wanderer.commons.domain.PromotedTrip;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TripPromotedEventHandlerTest {

    @Mock private PromotedTripRepository promotedTripRepository;

    @InjectMocks private TripPromotedEventHandler handler;

    @Test
    void handle_whenTripPromotedWithDonationLink_shouldPersistPromotedTrip() {
        // Given
        UUID id = UUID.randomUUID();
        UUID tripId = UUID.randomUUID();
        UUID promotedBy = UUID.randomUUID();
        String donationLink = "https://example.com/donate";
        Instant promotedAt = Instant.now();

        TripPromotedEvent event =
                TripPromotedEvent.builder()
                        .id(id)
                        .tripId(tripId)
                        .donationLink(donationLink)
                        .promotedBy(promotedBy)
                        .promotedAt(promotedAt)
                        .build();

        // When
        handler.handle(event);

        // Then
        ArgumentCaptor<PromotedTrip> captor = ArgumentCaptor.forClass(PromotedTrip.class);
        verify(promotedTripRepository).save(captor.capture());

        PromotedTrip saved = captor.getValue();
        assertThat(saved.getId()).isEqualTo(id);
        assertThat(saved.getTripId()).isEqualTo(tripId);
        assertThat(saved.getDonationLink()).isEqualTo(donationLink);
        assertThat(saved.getPromotedBy()).isEqualTo(promotedBy);
        assertThat(saved.getPromotedAt()).isEqualTo(promotedAt);
    }

    @Test
    void handle_whenTripPromotedWithoutDonationLink_shouldPersistPromotedTrip() {
        // Given
        UUID id = UUID.randomUUID();
        UUID tripId = UUID.randomUUID();
        UUID promotedBy = UUID.randomUUID();
        Instant promotedAt = Instant.now();

        TripPromotedEvent event =
                TripPromotedEvent.builder()
                        .id(id)
                        .tripId(tripId)
                        .donationLink(null)
                        .promotedBy(promotedBy)
                        .promotedAt(promotedAt)
                        .build();

        // When
        handler.handle(event);

        // Then
        ArgumentCaptor<PromotedTrip> captor = ArgumentCaptor.forClass(PromotedTrip.class);
        verify(promotedTripRepository).save(captor.capture());

        PromotedTrip saved = captor.getValue();
        assertThat(saved.getId()).isEqualTo(id);
        assertThat(saved.getTripId()).isEqualTo(tripId);
        assertThat(saved.getDonationLink()).isNull();
        assertThat(saved.getPromotedBy()).isEqualTo(promotedBy);
        assertThat(saved.getPromotedAt()).isEqualTo(promotedAt);
    }
}
