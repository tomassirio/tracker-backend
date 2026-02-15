package com.tomassirio.wanderer.command.event;

import com.tomassirio.wanderer.commons.domain.GeoLocation;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripPlanUpdatedEvent implements DomainEvent {
    private UUID tripPlanId;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private GeoLocation startLocation;
    private GeoLocation endLocation;
    private List<GeoLocation> waypoints;
}
