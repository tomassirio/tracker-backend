package com.tomassirio.wanderer.command.handler.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tomassirio.wanderer.command.event.TripPlanUpdatedEvent;
import com.tomassirio.wanderer.command.repository.TripPlanRepository;
import com.tomassirio.wanderer.command.service.TripPlanMetadataProcessor;
import com.tomassirio.wanderer.commons.domain.GeoLocation;
import com.tomassirio.wanderer.commons.domain.TripPlan;
import com.tomassirio.wanderer.commons.domain.TripPlanType;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TripPlanUpdatedEventPersistenceHandlerTest {

    @Mock private TripPlanRepository tripPlanRepository;
    @Mock private TripPlanMetadataProcessor metadataProcessor;

    @InjectMocks private TripPlanUpdatedEventPersistenceHandler handler;

    @Test
    void handle_shouldUpdateTripPlanFields() {
        UUID tripPlanId = UUID.randomUUID();
        LocalDate newStartDate = LocalDate.now().plusDays(2);
        LocalDate newEndDate = LocalDate.now().plusDays(10);
        GeoLocation newStartLocation = GeoLocation.builder().lat(51.5074).lon(-0.1278).build();
        GeoLocation newEndLocation = GeoLocation.builder().lat(48.8566).lon(2.3522).build();

        TripPlan existingPlan =
                TripPlan.builder()
                        .id(tripPlanId)
                        .name("Original Name")
                        .planType(TripPlanType.SIMPLE)
                        .userId(UUID.randomUUID())
                        .createdTimestamp(Instant.now())
                        .startDate(LocalDate.now())
                        .endDate(LocalDate.now().plusDays(5))
                        .metadata(new HashMap<>())
                        .build();

        TripPlanUpdatedEvent event =
                TripPlanUpdatedEvent.builder()
                        .tripPlanId(tripPlanId)
                        .name("Updated Name")
                        .startDate(newStartDate)
                        .endDate(newEndDate)
                        .startLocation(newStartLocation)
                        .endLocation(newEndLocation)
                        .waypoints(List.of())
                        .build();

        when(tripPlanRepository.findById(tripPlanId)).thenReturn(Optional.of(existingPlan));

        handler.handle(event);

        verify(tripPlanRepository).save(existingPlan);
        verify(metadataProcessor).applyMetadata(existingPlan, existingPlan.getMetadata());
        assertThat(existingPlan.getName()).isEqualTo("Updated Name");
        assertThat(existingPlan.getStartDate()).isEqualTo(newStartDate);
        assertThat(existingPlan.getEndDate()).isEqualTo(newEndDate);
    }

    @Test
    void handle_whenTripPlanNotFound_shouldThrowEntityNotFoundException() {
        UUID tripPlanId = UUID.randomUUID();
        TripPlanUpdatedEvent event =
                TripPlanUpdatedEvent.builder().tripPlanId(tripPlanId).name("Updated").build();

        when(tripPlanRepository.findById(tripPlanId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(event))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Trip plan not found");

        verify(tripPlanRepository, never()).save(any());
    }
}
