package com.tomassirio.wanderer.command.event;

import com.tomassirio.wanderer.commons.domain.GeoLocation;
import com.tomassirio.wanderer.commons.domain.TripPlanType;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripPlanCreatedEvent implements DomainEvent {
    private UUID tripPlanId;
    private UUID userId;
    private String name;
    private TripPlanType planType;
    private LocalDate startDate;
    private LocalDate endDate;
    private GeoLocation startLocation;
    private GeoLocation endLocation;
    private List<GeoLocation> waypoints;
    private Map<String, Object> metadata;
    private Instant createdTimestamp;
}
