package com.tomassirio.wanderer.command.handler.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tomassirio.wanderer.command.event.TripPlanUpdatedEvent;
import com.tomassirio.wanderer.command.repository.TripPlanRepository;
import com.tomassirio.wanderer.command.service.TripPlanMetadataProcessor;
import com.tomassirio.wanderer.commons.domain.GeoLocation;
import com.tomassirio.wanderer.commons.domain.TripPlan;
import com.tomassirio.wanderer.commons.domain.TripPlanType;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
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
        // Given
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

        // Validation is done in service layer, handler uses getReferenceById
        when(tripPlanRepository.getReferenceById(tripPlanId)).thenReturn(existingPlan);

        // When
        handler.handle(event);

        // Then
        verify(tripPlanRepository).save(existingPlan);
        verify(metadataProcessor).applyMetadata(existingPlan, existingPlan.getMetadata());
        assertThat(existingPlan.getName()).isEqualTo("Updated Name");
        assertThat(existingPlan.getStartDate()).isEqualTo(newStartDate);
        assertThat(existingPlan.getEndDate()).isEqualTo(newEndDate);
    }
}
