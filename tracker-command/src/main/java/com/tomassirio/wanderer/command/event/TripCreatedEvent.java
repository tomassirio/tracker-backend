package com.tomassirio.wanderer.command.event;

import com.tomassirio.wanderer.commons.domain.GeoLocation;
import java.time.Instant;
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
public class TripCreatedEvent implements DomainEvent {
    private UUID tripId;
    private String tripName;
    private UUID ownerId;
    private String visibility;
    private UUID tripPlanId;
    private Instant creationTimestamp;
    // Additional fields for createTripFromPlan
    private GeoLocation startLocation;
    private GeoLocation endLocation;
    private List<GeoLocation> waypoints;
    private Instant startTimestamp;
    private Instant endTimestamp;
}
