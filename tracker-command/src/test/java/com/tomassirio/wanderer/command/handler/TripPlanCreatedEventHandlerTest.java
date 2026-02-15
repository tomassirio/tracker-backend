package com.tomassirio.wanderer.command.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.tomassirio.wanderer.command.event.TripPlanCreatedEvent;
import com.tomassirio.wanderer.command.repository.TripPlanRepository;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TripPlanCreatedEventHandlerTest {

    @Mock private TripPlanRepository tripPlanRepository;

    @InjectMocks private TripPlanCreatedEventHandler handler;

    @Test
    void handle_shouldPersistTripPlan() {
        // Given
        UUID tripPlanId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(7);
        GeoLocation startLocation = GeoLocation.builder().lat(40.7128).lon(-74.0060).build();
        GeoLocation endLocation = GeoLocation.builder().lat(34.0522).lon(-118.2437).build();
        Instant createdTimestamp = Instant.now();

        TripPlanCreatedEvent event =
                TripPlanCreatedEvent.builder()
                        .tripPlanId(tripPlanId)
                        .userId(userId)
                        .name("Summer Trip")
                        .planType(TripPlanType.SIMPLE)
                        .startDate(startDate)
                        .endDate(endDate)
                        .startLocation(startLocation)
                        .endLocation(endLocation)
                        .waypoints(List.of())
                        .metadata(new HashMap<>())
                        .createdTimestamp(createdTimestamp)
                        .build();

        // When
        handler.handle(event);

        // Then
        ArgumentCaptor<TripPlan> captor = ArgumentCaptor.forClass(TripPlan.class);
        verify(tripPlanRepository).save(captor.capture());

        TripPlan saved = captor.getValue();
        assertThat(saved.getId()).isEqualTo(tripPlanId);
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getName()).isEqualTo("Summer Trip");
        assertThat(saved.getPlanType()).isEqualTo(TripPlanType.SIMPLE);
        assertThat(saved.getStartDate()).isEqualTo(startDate);
        assertThat(saved.getEndDate()).isEqualTo(endDate);
        assertThat(saved.getStartLocation()).isEqualTo(startLocation);
        assertThat(saved.getEndLocation()).isEqualTo(endLocation);
        assertThat(saved.getCreatedTimestamp()).isEqualTo(createdTimestamp);
    }
}
